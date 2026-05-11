package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.request.liquidacion.IniciarLiquidacionRequestDto;
import co.fintra.financiero.dto.response.liquidacion.LiquidacionDetalleItemDto;
import co.fintra.financiero.dto.response.liquidacion.LiquidacionMensualResponseDto;
import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.services.impl.liquidacion.*;
import co.fintra.financiero.services.interfaces.ILiquidacionService;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LiquidacionServiceImpl implements ILiquidacionService {

  private static final String[] MESES_ES = {
      "", "enero","febrero","marzo","abril","mayo","junio",
      "julio","agosto","septiembre","octubre","noviembre","diciembre"
  };

  private final ILiquidacionMensualRepository       liqRepo;
  private final ILiquidacionMensualDetalleRepository detalleRepo;
  private final IOperacionRepository                operacionRepo;
  private final IEmpresaRepository                  empresaRepo;
  private final IUsuarioRepository                  usuarioRepo;
  private final MotorLiquidacionService             motor;
  private final PlantillaErpGeneratorRegistry       generatorRegistry;

  @Override
  public LiquidacionMensualResponseDto iniciar(IniciarLiquidacionRequestDto req) {
    if (liqRepo.existsByAnioAndMes(req.getAnio(), req.getMes()))
      throw new BusinessException("Ya existe una liquidación para " + req.getMes() + "/" + req.getAnio());

    YearMonth ym = YearMonth.of(req.getAnio(), req.getMes());
    LiquidacionMensualEntity liq = LiquidacionMensualEntity.builder()
        .anio(req.getAnio())
        .mes(req.getMes())
        .fechaCorte(ym.atEndOfMonth())
        .estado("BORRADOR")
        .totalInteresesLiquidados(BigDecimal.ZERO)
        .build();

    liq = liqRepo.save(liq);
    log.info("Liquidación iniciada para {}/{}", req.getMes(), req.getAnio());
    return toDto(liq, List.of());
  }

  @Override
  public LiquidacionMensualResponseDto calcular(Long id) {
    LiquidacionMensualEntity liq = findOrThrow(id);

    if ("APROBADA".equals(liq.getEstado()) || "CONTABILIZADA".equals(liq.getEstado()))
      throw new BusinessException("No se puede recalcular una liquidación " + liq.getEstado());

    liq.setEstado("BORRADOR");
    motor.calcular(liq);
    liq.setEstado("PENDIENTE_APROBACION");
    liq = liqRepo.save(liq);

    return toDto(liq, detalleRepo.findAllByLiquidacionId(liq.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<LiquidacionMensualResponseDto> listar() {
    return liqRepo.findAllByOrderByAnioDescMesDesc().stream()
        .map(l -> toDto(l, List.of()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public LiquidacionMensualResponseDto obtener(Long id) {
    LiquidacionMensualEntity liq = findOrThrow(id);
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  public LiquidacionMensualResponseDto aprobar(Long id) {
    LiquidacionMensualEntity liq = findOrThrow(id);

    if (!"PENDIENTE_APROBACION".equals(liq.getEstado()))
      throw new BusinessException("Solo se puede aprobar una liquidación en estado PENDIENTE_APROBACION");

    UsuarioEntity usuario = currentUser();
    liq.setEstado("APROBADA");
    liq.setAprobadaAt(OffsetDateTime.now());
    liq.setAprobadaPor(usuario != null ? usuario.getId() : null);
    liq = liqRepo.save(liq);

    log.info("Liquidación {}/{} aprobada por {}", liq.getMes(), liq.getAnio(),
        usuario != null ? usuario.getNombre() : "sistema");
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  public LiquidacionMensualResponseDto revertir(Long id) {
    LiquidacionMensualEntity liq = findOrThrow(id);

    if ("APROBADA".equals(liq.getEstado()) || "CONTABILIZADA".equals(liq.getEstado()))
      throw new BusinessException("No se puede revertir una liquidación " + liq.getEstado());

    motor.revertir(liq);
    liq.setEstado("BORRADOR");
    liq = liqRepo.save(liq);

    log.info("Liquidación {}/{} revertida a BORRADOR", liq.getMes(), liq.getAnio());
    return toDto(liq, List.of());
  }

  @Override
  public LiquidacionMensualResponseDto marcarContabilizada(Long id) {
    LiquidacionMensualEntity liq = findOrThrow(id);

    if (!"APROBADA".equals(liq.getEstado()))
      throw new BusinessException("Solo se puede marcar como contabilizada una liquidación APROBADA");

    liq.setEstado("CONTABILIZADA");
    liq = liqRepo.save(liq);
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] descargarPlantilla(Long id, Long empresaId) {
    LiquidacionMensualEntity liq = findOrThrow(id);

    if (!"APROBADA".equals(liq.getEstado()) && !"CONTABILIZADA".equals(liq.getEstado()))
      throw new BusinessException("Las plantillas solo están disponibles para liquidaciones APROBADAS");

    EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(empresaId)
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));

    List<LiquidacionMensualDetalleEntity> detallesEmpresa = detalleRepo
        .findAllByLiquidacionId(id).stream()
        .filter(d -> {
          OperacionEntity op = operacionRepo.findByIdAndDeletedAtIsNull(d.getOperacionId()).orElse(null);
          return op != null && op.getEmpresaPrestataria().getId().equals(empresaId);
        })
        .collect(Collectors.toList());

    PlantillaErpGenerator gen = generatorRegistry.getForErp(empresa.getErpUtilizado());
    PlantillaErpResult resultado = gen.generar(liq, detallesEmpresa, empresa.getRazonSocial());
    return resultado.getContenido().getBytes(StandardCharsets.UTF_8);
  }

  // ── helpers ──────────────────────────────────────────────────────

  private LiquidacionMensualEntity findOrThrow(Long id) {
    return liqRepo.findById(id)
        .orElseThrow(() -> new CustomException("Liquidación no encontrada: " + id, HttpStatus.NOT_FOUND));
  }

  private UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private LiquidacionMensualResponseDto toDto(LiquidacionMensualEntity liq,
                                               List<LiquidacionMensualDetalleEntity> detalles) {
    BigDecimal retFuente = detalles.isEmpty()
        ? detalleRepo.sumRetencionFuente(liq.getId())
        : detalles.stream().map(LiquidacionMensualDetalleEntity::getRetencionFuenteAplicada)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal retIca = detalles.isEmpty()
        ? detalleRepo.sumRetencionIca(liq.getId())
        : detalles.stream().map(LiquidacionMensualDetalleEntity::getRetencionIcaAplicada)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal neto = liq.getTotalInteresesLiquidados().subtract(retFuente).subtract(retIca);

    String aprobadoPorNombre = null;
    if (liq.getAprobadaPor() != null) {
      aprobadoPorNombre = usuarioRepo.findById(liq.getAprobadaPor())
          .map(UsuarioEntity::getNombre).orElse(null);
    }

    List<LiquidacionDetalleItemDto> detalleDto = detalles.stream()
        .map(d -> toDetalleDto(d))
        .collect(Collectors.toList());

    return LiquidacionMensualResponseDto.builder()
        .id(liq.getId())
        .anio(liq.getAnio())
        .mes(liq.getMes())
        .periodo(MESES_ES[liq.getMes()] + " " + liq.getAnio())
        .fechaCorte(liq.getFechaCorte())
        .estado(liq.getEstado())
        .totalInteresesLiquidados(liq.getTotalInteresesLiquidados())
        .totalRetencionFuente(retFuente)
        .totalRetencionIca(retIca)
        .totalNetoCobrar(neto)
        .aprobadaPorNombre(aprobadoPorNombre)
        .aprobadaAt(liq.getAprobadaAt())
        .createdAt(liq.getCreatedAt())
        .detalle(detalleDto)
        .build();
  }

  private LiquidacionDetalleItemDto toDetalleDto(LiquidacionMensualDetalleEntity d) {
    OperacionEntity op = operacionRepo.findByIdAndDeletedAtIsNull(d.getOperacionId()).orElse(null);
    BigDecimal neto = d.getInteresesPeriodo()
        .subtract(d.getRetencionFuenteAplicada())
        .subtract(d.getRetencionIcaAplicada());

    return LiquidacionDetalleItemDto.builder()
        .id(d.getId())
        .operacionId(d.getOperacionId())
        .referencia(op != null ? op.getReferencia() : null)
        .empresaPrestatariaNombre(op != null ? op.getEmpresaPrestataria().getRazonSocial() : null)
        .empresaPrestamistaNombre(op != null ? op.getEmpresaPrestamista().getRazonSocial() : null)
        .interesesPeriodo(d.getInteresesPeriodo())
        .retencionFuenteAplicada(d.getRetencionFuenteAplicada())
        .retencionIcaAplicada(d.getRetencionIcaAplicada())
        .netoCobrar(neto)
        .build();
  }
}
