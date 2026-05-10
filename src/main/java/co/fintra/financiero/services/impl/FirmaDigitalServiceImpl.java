package co.fintra.financiero.services.impl;

import co.fintra.financiero.config.ThomasSigneProperties;
import co.fintra.financiero.dto.response.firma.FirmaEstadoDto;
import co.fintra.financiero.models.entity.EventoPipelineEntity;
import co.fintra.financiero.models.entity.OperacionEntity;
import co.fintra.financiero.models.entity.ThomasSigneSolicitudEntity;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.services.interfaces.IFirmaDigitalService;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FirmaDigitalServiceImpl implements IFirmaDigitalService {

  private final IThomasSigneSolicitudRepository solicitudRepo;
  private final IOperacionRepository            operacionRepo;
  private final IEventoPipelineRepository        eventoRepo;
  private final IUsuarioRepository              usuarioRepo;
  private final ThomasSigneProperties            props;

  @Override
  public FirmaEstadoDto iniciarFirma(Long operacionId) {
    OperacionEntity op = findOperacionOrThrow(operacionId);
    if (!"FD".equals(op.getEstadoPipeline()))
      throw new BusinessException("La operación debe estar en estado FD para iniciar firma");

    String idempotencyKey = "FIRMA-" + operacionId;
    if (solicitudRepo.existsByIdempotencyKey(idempotencyKey)) {
      return toDto(solicitudRepo.findByIdempotencyKey(idempotencyKey).orElseThrow());
    }

    String destinatario = op.getEmpresaPrestataria().getRepresentanteLegalEmail() != null
        ? op.getEmpresaPrestataria().getRepresentanteLegalEmail()
        : "representante@" + op.getEmpresaPrestataria().getCodigoInterno().toLowerCase() + ".co";

    String docUrl = generarYAlmacenarPdf(op);

    ThomasSigneSolicitudEntity solicitud = ThomasSigneSolicitudEntity.builder()
        .operacionId(operacionId)
        .documentoUrl(docUrl)
        .destinatarioEmail(destinatario)
        .estado("ENVIADA")
        .enviadoAt(OffsetDateTime.now())
        .idempotencyKey(idempotencyKey)
        .build();

    solicitud = solicitudRepo.save(solicitud);
    op.setFirmaDigitalDocumentoId(solicitud.getId());
    op.setFirmaDigitalAt(OffsetDateTime.now());
    operacionRepo.save(op);

    enviarAThomasSigne(solicitud, op);
    log.info("Firma digital iniciada para operación {} — solicitud {}", operacionId, solicitud.getId());

    return toDto(solicitud);
  }

  @Override
  public FirmaEstadoDto reenviarFirma(Long operacionId) {
    ThomasSigneSolicitudEntity solicitud = solicitudRepo.findByOperacionId(operacionId)
        .orElseThrow(() -> new CustomException("No existe solicitud de firma para esta operación", HttpStatus.NOT_FOUND));

    if ("FIRMADA".equals(solicitud.getEstado()))
      throw new BusinessException("El documento ya fue firmado");

    solicitud.setEstado("ENVIADA");
    solicitud.setEnviadoAt(OffsetDateTime.now());
    solicitudRepo.save(solicitud);

    OperacionEntity op = findOperacionOrThrow(operacionId);
    enviarAThomasSigne(solicitud, op);
    log.info("Firma reenviada para operación {}", operacionId);

    return toDto(solicitud);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FirmaEstadoDto> consultarEstado(Long operacionId) {
    return solicitudRepo.findByOperacionId(operacionId).map(this::toDto);
  }

  @Override
  public void procesarWebhook(String payload, String signature) {
    validarHmac(payload, signature);

    // Parse idempotency key and evento from payload (JSON simple parsing)
    String idempotencyKey = extractField(payload, "idempotency_key");
    String evento          = extractField(payload, "evento");
    String transactionId   = extractField(payload, "transaction_id");

    if (idempotencyKey == null || evento == null) {
      log.warn("Webhook Thomas Signe sin idempotency_key o evento en payload");
      return;
    }

    // Find solicitud by idempotency key or transaction ID
    Optional<ThomasSigneSolicitudEntity> opt = solicitudRepo.findByIdempotencyKey(idempotencyKey);
    if (opt.isEmpty()) {
      log.warn("Webhook: solicitud no encontrada para key={}", idempotencyKey);
      return;
    }

    ThomasSigneSolicitudEntity solicitud = opt.get();

    // Idempotente: si ya está en estado final, ignorar
    if ("FIRMADA".equals(solicitud.getEstado()) && "documento.firmado".equals(evento)) {
      log.info("Webhook idempotente: solicitud {} ya estaba FIRMADA", solicitud.getId());
      return;
    }

    solicitud.setWebhookPayload(payload);

    OperacionEntity op = findOperacionOrThrow(solicitud.getOperacionId());
    String estadoAnteriorOp = op.getEstadoPipeline();

    switch (evento) {
      case "documento.firmado" -> {
        solicitud.setEstado("FIRMADA");
        solicitud.setFirmadoAt(OffsetDateTime.now());
        op.setEstadoPipeline("DS");
        op.setFirmaDigitalAt(OffsetDateTime.now());
        registrarEvento(op, estadoAnteriorOp, "DS", "Documento firmado via Thomas Signe. TX: " + transactionId);
        log.info("Operación {} transicionada FD→DS por firma Thomas Signe", op.getId());
      }
      case "documento.rechazado" -> {
        solicitud.setEstado("RECHAZADA");
        op.setEstadoPipeline("RECHAZADA");
        registrarEvento(op, estadoAnteriorOp, "RECHAZADA", "Firma rechazada por el destinatario via Thomas Signe");
        log.warn("Operación {} RECHAZADA por firma Thomas Signe", op.getId());
      }
      case "documento.expirado" -> {
        solicitud.setEstado("EXPIRADA");
        log.warn("Firma expirada para operación {}. Botón 'Reenviar' disponible.", op.getId());
      }
      default -> log.warn("Evento Thomas Signe desconocido: {}", evento);
    }

    solicitudRepo.save(solicitud);
    operacionRepo.save(op);
  }

  // ─────────────────────────────────── privados

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 4))
  private void enviarAThomasSigne(ThomasSigneSolicitudEntity solicitud, OperacionEntity op) {
    // MOCK: en producción aquí va la llamada real al API de Thomas Signe
    log.info("[MOCK] Enviando a Thomas Signe — operación: {}, destinatario: {}, doc: {}",
        op.getReferencia(), solicitud.getDestinatarioEmail(), solicitud.getDocumentoUrl());
    // Simular respuesta mock
    log.info("[MOCK] Thomas Signe respondió OK — transaction_id: mock-{}", solicitud.getId());
  }

  private String generarYAlmacenarPdf(OperacionEntity op) {
    try {
      String dir = props.getStoragePath() + "/" + op.getId();
      new File(dir).mkdirs();
      String filePath = dir + "/acuerdo_prestamo.pdf";

      byte[] pdf = generarPdf(op);
      try (var fos = new FileOutputStream(filePath)) {
        fos.write(pdf);
      }

      log.debug("PDF generado: {}", filePath);
      return "/api/v1/operaciones/" + op.getId() + "/firma/documentos/acuerdo";

    } catch (Exception e) {
      log.error("Error generando PDF para operación {}: {}", op.getId(), e.getMessage());
      throw new BusinessException("Error al generar el documento de firma");
    }
  }

  private byte[] generarPdf(OperacionEntity op) throws Exception {
    try (var baos = new ByteArrayOutputStream()) {
      Document doc = new Document(PageSize.A4, 60, 60, 60, 60);
      PdfWriter.getInstance(doc, baos);
      doc.open();

      Font fontTitle = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
      Font fontBold  = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
      Font fontNorm  = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
      Font fontSmall = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

      doc.add(new Paragraph("ACUERDO DE PRÉSTAMO INTERCOMPAÑÍA", fontTitle));
      doc.add(new Paragraph("Referencia: " + op.getReferencia(), fontBold));
      doc.add(Chunk.NEWLINE);

      doc.add(new Paragraph("PARTES", fontBold));
      doc.add(new Paragraph("Prestamista: " + op.getEmpresaPrestamista().getRazonSocial()
          + " — NIT " + op.getEmpresaPrestamista().getNit(), fontNorm));
      doc.add(new Paragraph("Prestataria: " + op.getEmpresaPrestataria().getRazonSocial()
          + " — NIT " + op.getEmpresaPrestataria().getNit(), fontNorm));
      doc.add(Chunk.NEWLINE);

      doc.add(new Paragraph("CONDICIONES", fontBold));
      doc.add(new Paragraph("Tipo de interés: " + op.getCobraInteres(), fontNorm));
      if (op.getMontoEstimado() != null)
        doc.add(new Paragraph("Monto estimado: $ " + op.getMontoEstimado().toPlainString(), fontNorm));
      doc.add(new Paragraph("Fecha: " + LocalDate.now(), fontNorm));
      doc.add(new Paragraph("N° Documento soporte: " + op.getNumDocumentoSoporte(), fontNorm));
      doc.add(Chunk.NEWLINE);

      doc.add(new Paragraph("OBSERVACIONES", fontBold));
      doc.add(new Paragraph(op.getObservaciones(), fontNorm));
      doc.add(Chunk.NEWLINE);

      doc.add(new Paragraph("Generado automáticamente por Fintra Financiero — " + LocalDate.now(), fontSmall));

      doc.close();
      return baos.toByteArray();
    }
  }

  private void validarHmac(String payload, String signature) {
    try {
      if (signature == null || signature.isBlank())
        throw new CustomException("Firma HMAC ausente en el webhook", HttpStatus.UNAUTHORIZED);

      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec keySpec = new SecretKeySpec(
          props.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(keySpec);
      byte[] computed = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      String expected = HexFormat.of().formatHex(computed);

      if (!expected.equalsIgnoreCase(signature.replaceFirst("sha256=", "")))
        throw new CustomException("Firma HMAC inválida", HttpStatus.UNAUTHORIZED);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException("Error validando firma HMAC: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
  }

  private String extractField(String json, String field) {
    String search = "\"" + field + "\"";
    int idx = json.indexOf(search);
    if (idx < 0) return null;
    int colon = json.indexOf(':', idx + search.length());
    if (colon < 0) return null;
    int start = json.indexOf('"', colon + 1);
    if (start < 0) return null;
    int end = json.indexOf('"', start + 1);
    if (end < 0) return null;
    return json.substring(start + 1, end);
  }

  private void registrarEvento(OperacionEntity op, String anterior, String nuevo, String obs) {
    eventoRepo.save(EventoPipelineEntity.builder()
        .operacion(op)
        .estadoAnterior(anterior)
        .estadoNuevo(nuevo)
        .usuario(currentUser())
        .observacion(obs)
        .ocurridoAt(OffsetDateTime.now())
        .build());
  }

  private OperacionEntity findOperacionOrThrow(Long id) {
    return operacionRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Operación no encontrada", HttpStatus.NOT_FOUND));
  }

  private co.fintra.financiero.models.entity.UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private FirmaEstadoDto toDto(ThomasSigneSolicitudEntity s) {
    return FirmaEstadoDto.builder()
        .solicitudId(s.getId())
        .operacionId(s.getOperacionId())
        .estado(s.getEstado())
        .destinatarioEmail(s.getDestinatarioEmail())
        .documentoUrl(s.getDocumentoUrl())
        .enviadoAt(s.getEnviadoAt())
        .firmadoAt(s.getFirmadoAt())
        .createdAt(s.getCreatedAt())
        .build();
  }
}
