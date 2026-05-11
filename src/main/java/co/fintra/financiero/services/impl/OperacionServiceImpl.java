package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.request.operaciones.CrearOperacionRequestDto;
import co.fintra.financiero.dto.response.operaciones.*;
import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.services.interfaces.IFirmaDigitalService;
import co.fintra.financiero.services.interfaces.IOperacionService;
import co.fintra.financiero.services.interfaces.ITasaPeriodoService;
import co.fintra.financiero.infrastructure.integration.events.OperacionPipelineEvent;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OperacionServiceImpl implements IOperacionService {

  private final IOperacionRepository     operacionRepo;
  private final ITramoRepository         tramoRepo;
  private final IEventoPipelineRepository eventoRepo;
  private final IEmpresaRepository       empresaRepo;
  private final IEmpresaCuentaBancariaRepository cuentaRepo;
  private final ITasaEspecialEmpresaRepository   tasaEspecialRepo;
  private final IUsuarioRepository       usuarioRepo;
  private final ITasaPeriodoService      tasaPeriodoService;
  private final IFirmaDigitalService     firmaDigitalService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public Page<OperacionListItemDto> listar(String estado, Long prestamistaId,
                                            Long prestatariaId, String referencia,
                                            Pageable pageable) {
    return operacionRepo.buscar(estado, prestamistaId, prestatariaId, referencia, pageable)
        .map(this::toListItem);
  }

  @Override
  @Transactional(readOnly = true)
  public OperacionResponseDto obtener(Long id) {
    OperacionEntity op = findOrThrow(id);
    List<EventoPipelineDto> eventos = eventoRepo.findAllByOperacionIdOrderByOcurridoAtAsc(id)
        .stream().map(this::toEventoDto).collect(Collectors.toList());
    Optional<AvisoTramoAnteriorDto> aviso = calcularAvisoTramoAnterior(
        op.getEmpresaPrestataria().getId());
    return toResponseDto(op, eventos, aviso.orElse(null));
  }

  @Override
  public OperacionResponseDto crear(CrearOperacionRequestDto req) {
    // 1. Bloqueo global de tasas
    var bloqueo = tasaPeriodoService.evaluarBloqueoSistema();
    if ("BLOQUEADO_GLOBAL".equals(bloqueo.getEstado()))
      throw new BusinessException("No se puede crear la operación: " + bloqueo.getMotivo());

    // 2. Validar empresas
    EmpresaEntity prestamista = findEmpresaActiva(req.getEmpresaPrestamistaId());
    EmpresaEntity prestataria  = findEmpresaActiva(req.getEmpresaPrestatariaId());

    if (prestamista.getId().equals(prestataria.getId()))
      throw new BusinessException("Una empresa no puede prestarse a sí misma");

    validarRolEmpresa(prestamista, "PRESTAMISTA", "prestamista");
    validarRolEmpresa(prestataria,  "PRESTATARIA", "prestataria");

    // 3. Validar interés y tasas
    validarCobraInteres(req.getCobraInteres(), prestataria);

    // 4. Validar cuentas bancarias
    EmpresaCuentaBancariaEntity cuentaOrigen  = null;
    EmpresaCuentaBancariaEntity cuentaDestino = null;

    if (req.getCuentaOrigenId() != null) {
      cuentaOrigen = findCuentaActiva(req.getCuentaOrigenId());
      if (!cuentaOrigen.getEmpresa().getId().equals(prestamista.getId()))
        throw new BusinessException("La cuenta origen debe pertenecer a la empresa prestamista");
    }
    if (req.getCuentaDestinoId() != null) {
      cuentaDestino = findCuentaActiva(req.getCuentaDestinoId());
      if (!cuentaDestino.getEmpresa().getId().equals(prestataria.getId()))
        throw new BusinessException("La cuenta destino debe pertenecer a la empresa prestataria");
    }

    // 5. Aviso tramo anterior (antes de crear)
    Optional<AvisoTramoAnteriorDto> aviso = calcularAvisoTramoAnterior(prestataria.getId());

    // 6. Crear operación
    OperacionEntity operacion = OperacionEntity.builder()
        .empresaPrestamista(prestamista)
        .empresaPrestataria(prestataria)
        .cobraInteres(req.getCobraInteres())
        .cuentaOrigen(cuentaOrigen)
        .cuentaDestino(cuentaDestino)
        .montoEstimado(req.getMontoEstimado())
        .observaciones(req.getObservaciones())
        .numDocumentoSoporte(req.getNumDocumentoSoporte())
        .estadoPipeline("CR")
        .fechaCreacion(LocalDate.now())
        .build();

    operacion = operacionRepo.save(operacion);

    // 7. Registrar evento inicial
    registrarEvento(operacion, null, "CR", null);

    // Re-fetch para obtener referencia generada por trigger
    operacion = operacionRepo.findByIdAndDeletedAtIsNull(operacion.getId()).orElseThrow();

    List<EventoPipelineDto> eventos = eventoRepo
        .findAllByOperacionIdOrderByOcurridoAtAsc(operacion.getId())
        .stream().map(this::toEventoDto).collect(Collectors.toList());

    return toResponseDto(operacion, eventos, aviso.orElse(null));
  }

  @Override
  public OperacionResponseDto editar(Long id, CrearOperacionRequestDto req) {
    OperacionEntity op = findOrThrow(id);
    if (!"CR".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se pueden editar operaciones en estado CR");

    EmpresaEntity prestamista = findEmpresaActiva(req.getEmpresaPrestamistaId());
    EmpresaEntity prestataria  = findEmpresaActiva(req.getEmpresaPrestatariaId());
    if (prestamista.getId().equals(prestataria.getId()))
      throw new BusinessException("Una empresa no puede prestarse a sí misma");

    validarRolEmpresa(prestamista, "PRESTAMISTA", "prestamista");
    validarRolEmpresa(prestataria,  "PRESTATARIA", "prestataria");
    validarCobraInteres(req.getCobraInteres(), prestataria);

    op.setEmpresaPrestamista(prestamista);
    op.setEmpresaPrestataria(prestataria);
    op.setCobraInteres(req.getCobraInteres());
    op.setMontoEstimado(req.getMontoEstimado());
    op.setObservaciones(req.getObservaciones());
    op.setNumDocumentoSoporte(req.getNumDocumentoSoporte());

    if (req.getCuentaOrigenId() != null) {
      var co = findCuentaActiva(req.getCuentaOrigenId());
      if (!co.getEmpresa().getId().equals(prestamista.getId()))
        throw new BusinessException("La cuenta origen debe pertenecer a la empresa prestamista");
      op.setCuentaOrigen(co);
    }
    if (req.getCuentaDestinoId() != null) {
      var cd = findCuentaActiva(req.getCuentaDestinoId());
      if (!cd.getEmpresa().getId().equals(prestataria.getId()))
        throw new BusinessException("La cuenta destino debe pertenecer a la empresa prestataria");
      op.setCuentaDestino(cd);
    }

    op = operacionRepo.save(op);
    return toResponseDto(op, List.of(), null);
  }

  @Override
  public OperacionResponseDto cancelar(Long id, String motivo) {
    OperacionEntity op = findOrThrow(id);
    if (!List.of("CR","AI").contains(op.getEstadoPipeline()))
      throw new BusinessException("Solo se pueden cancelar operaciones en CR o AI");

    String estadoAnterior = op.getEstadoPipeline();
    op.setEstadoPipeline("CANCELADA");
    op = operacionRepo.save(op);
    registrarEvento(op, estadoAnterior, "CANCELADA", motivo);

    return toResponseDto(op, List.of(), null);
  }

  @Override
  public OperacionResponseDto enviarAprobacion(Long id) {
    OperacionEntity op = findOrThrow(id);
    if (!"CR".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede enviar a aprobación desde estado CR");
    if (op.getCuentaOrigen() == null || op.getCuentaDestino() == null)
      throw new BusinessException("Se requieren cuentas bancarias origen y destino para enviar a aprobación");

    op.setEstadoPipeline("AI");
    op = operacionRepo.save(op);
    registrarEvento(op, "CR", "AI", "Enviado a aprobación interna");
    eventPublisher.publishEvent(new OperacionPipelineEvent(this, op.getId(), op.getReferencia(),
        "AI", OperacionPipelineEvent.ENVIADA_APROBACION));
    return toResponseDto(op, List.of(), null);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AvisoTramoAnteriorDto> calcularAvisoTramoAnterior(Long empresaPrestatariaId) {
    var operacionesDs = operacionRepo
        .findAllByEmpresaPrestatariaIdAndEstadoPipelineAndDeletedAtIsNull(empresaPrestatariaId, "DS");

    for (OperacionEntity op : operacionesDs) {
      var tramoOpt = tramoRepo.findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(
          op.getId(), "EN_CURSO");
      if (tramoOpt.isPresent()) {
        TramoEntity tramo = tramoOpt.get();
        long dias = ChronoUnit.DAYS.between(tramo.getFechaDesde(), LocalDate.now());
        if (dias < 1) dias = 1;

        BigDecimal interes = tramo.getSaldoCapital()
            .multiply(tramo.getTasaPorcentajeMensual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN))
            .multiply(BigDecimal.valueOf(dias).divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_EVEN))
            .setScale(2, RoundingMode.HALF_EVEN);

        return Optional.of(AvisoTramoAnteriorDto.builder()
            .operacionId(op.getId())
            .referencia(op.getReferencia())
            .tramoId(tramo.getId())
            .saldoCapital(tramo.getSaldoCapital())
            .fechaDesdeTramo(tramo.getFechaDesde())
            .diasTranscurridos((int) dias)
            .tasaMensual(tramo.getTasaPorcentajeMensual())
            .tipoTasa(tramo.getTipoTasa())
            .interesEstimado(interes)
            .build());
      }
    }
    return Optional.empty();
  }

  // ── AI: Aprobación interna ──────────────────────────────────────

  @Override
  public OperacionResponseDto aprobarInterna(Long id, String observacion) {
    OperacionEntity op = findOrThrow(id);
    if (!"AI".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede aprobar en estado AI");

    op.setEstadoPipeline("AE");
    op.setAprobacionInternaAt(OffsetDateTime.now());
    op.setAprobacionInternaUsuario(currentUser());
    op.setAprobacionInternaObservacion(observacion);
    op = operacionRepo.save(op);
    registrarEvento(op, "AI", "AE", observacion);
    eventPublisher.publishEvent(new OperacionPipelineEvent(this, op.getId(), op.getReferencia(),
        "AE", OperacionPipelineEvent.APROBADA_INTERNAMENTE));
    return toResponseDto(op, List.of(), null);
  }

  @Override
  public OperacionResponseDto devolverDesdeAI(Long id, String observacion) {
    OperacionEntity op = findOrThrow(id);
    if (!"AI".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede devolver desde estado AI");
    if (observacion == null || observacion.trim().length() < 20)
      throw new BusinessException("La observación de devolución debe tener al menos 20 caracteres");

    op.setEstadoPipeline("CR");
    op = operacionRepo.save(op);
    registrarEvento(op, "AI", "CR", "DEVOLUCIÓN: " + observacion);

    return toResponseDto(op, List.of(), null);
  }

  @Override
  public OperacionResponseDto rechazarInterna(Long id, String motivo) {
    OperacionEntity op = findOrThrow(id);
    if (!"AI".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede rechazar desde estado AI");
    if (motivo == null || motivo.isBlank())
      throw new BusinessException("El motivo de rechazo es obligatorio");

    op.setEstadoPipeline("RECHAZADA");
    op = operacionRepo.save(op);
    registrarEvento(op, "AI", "RECHAZADA", motivo);

    return toResponseDto(op, List.of(), null);
  }

  // ── AE: Aceptación empresa ──────────────────────────────────────

  @Override
  public OperacionResponseDto aceptarEmpresa(Long id, String observacion) {
    OperacionEntity op = findOrThrow(id);
    if (!"AE".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede aceptar en estado AE");

    validarAccesoEmpresa(op);

    op.setEstadoPipeline("FD");
    op.setAceptacionEmpresaAt(OffsetDateTime.now());
    op.setAceptacionEmpresaUsuario(currentUser());
    op.setAceptacionEmpresaObservacion(observacion);
    op = operacionRepo.save(op);
    registrarEvento(op, "AE", "FD", observacion);
    eventPublisher.publishEvent(new OperacionPipelineEvent(this, op.getId(), op.getReferencia(),
        "FD", OperacionPipelineEvent.ACEPTADA_EMPRESA));

    // Iniciar firma digital automáticamente al aceptar
    try {
      firmaDigitalService.iniciarFirma(op.getId());
    } catch (Exception e) {
      log.warn("No se pudo iniciar firma automática para op {}: {}", op.getId(), e.getMessage());
    }

    return toResponseDto(op, List.of(), null);
  }

  @Override
  public OperacionResponseDto rechazarEmpresa(Long id, String motivo) {
    OperacionEntity op = findOrThrow(id);
    if (!"AE".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede rechazar en estado AE");
    if (motivo == null || motivo.isBlank())
      throw new BusinessException("El motivo de rechazo es obligatorio");

    validarAccesoEmpresa(op);

    op.setEstadoPipeline("RECHAZADA");
    op = operacionRepo.save(op);
    registrarEvento(op, "AE", "RECHAZADA", motivo);

    return toResponseDto(op, List.of(), null);
  }

  // ── Bandejas ────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<OperacionListItemDto> listarPendientesAprobacion() {
    return operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("AI")
        .stream().map(this::toListItem).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OperacionListItemDto> listarPendientesAceptacion() {
    UsuarioEntity usuario = currentUser();
    if (usuario == null) return List.of();

    boolean isAdmin = hasAuthority("ADMIN");
    List<Long> empresaIds = isAdmin
        ? List.of()
        : usuarioRepo.findEmpresaIdsByUsuarioId(usuario.getId());

    if (!isAdmin && empresaIds.isEmpty()) return List.of();

    List<OperacionEntity> ops = isAdmin
        ? operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("AE")
        : operacionRepo.findPendientesAceptacion(empresaIds);

    return ops.stream().map(this::toListItem).collect(Collectors.toList());
  }

  // ── Historial ────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<EventoPipelineDto> historial(Long id) {
    findOrThrow(id);
    return eventoRepo.findAllByOperacionIdOrderByOcurridoAtAsc(id)
        .stream().map(this::toEventoDto).collect(Collectors.toList());
  }

  // ──────────────────────────────────────── helpers

  private void validarRolEmpresa(EmpresaEntity empresa, String rolBase, String tipo) {
    String rol = empresa.getRolPermitido();
    if (!rol.equals(rolBase) && !rol.equals("AMBOS"))
      throw new BusinessException("La empresa " + empresa.getRazonSocial() +
          " no puede actuar como " + tipo + " (rol: " + rol + ")");
  }

  private void validarCobraInteres(String cobraInteres, EmpresaEntity prestataria) {
    if ("SI_COMERCIAL".equals(cobraInteres)) {
      var vigentes = tasaPeriodoService.listarVigentesHoy();
      boolean tieneComercial = vigentes.stream()
          .anyMatch(t -> "COMERCIAL_VIGENTE".equals(t.getTipoTasa()));
      if (!tieneComercial)
        throw new BusinessException("No existe tasa COMERCIAL_VIGENTE vigente para hoy. Registre la tasa del período.");
    }
    if ("SI_ESPECIAL".equals(cobraInteres)) {
      if (!Boolean.TRUE.equals(prestataria.getAplicaTasaEspecial()))
        throw new BusinessException("La empresa prestataria no tiene habilitadas las tasas especiales");
      boolean tieneEspecial = tasaEspecialRepo
          .existsByEmpresaIdAndEstadoAndDeletedAtIsNull(prestataria.getId(), "VIGENTE");
      if (!tieneEspecial)
        throw new BusinessException("La empresa prestataria no tiene tasa especial VIGENTE. Solicite y apruebe una tasa especial primero.");
    }
  }

  private EmpresaEntity findEmpresaActiva(Long id) {
    EmpresaEntity e = empresaRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Empresa no encontrada: " + id, HttpStatus.NOT_FOUND));
    if (!"ACTIVA".equals(e.getEstado()))
      throw new BusinessException("La empresa " + e.getRazonSocial() + " está inactiva");
    return e;
  }

  private EmpresaCuentaBancariaEntity findCuentaActiva(Long id) {
    EmpresaCuentaBancariaEntity c = cuentaRepo.findById(id)
        .orElseThrow(() -> new CustomException("Cuenta bancaria no encontrada", HttpStatus.NOT_FOUND));
    if (!Boolean.TRUE.equals(c.getActiva()))
      throw new BusinessException("La cuenta bancaria seleccionada no está activa");
    return c;
  }

  private OperacionEntity findOrThrow(Long id) {
    return operacionRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Operación no encontrada", HttpStatus.NOT_FOUND));
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

  private UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private boolean hasAuthority(String authority) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return false;
    return auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(authority));
  }

  private void validarAccesoEmpresa(OperacionEntity op) {
    if (hasAuthority("ADMIN")) return;
    UsuarioEntity usuario = currentUser();
    if (usuario == null)
      throw new CustomException("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
    Long prestatariaId = op.getEmpresaPrestataria().getId();
    boolean vinculado = usuarioRepo.existsVinculoUsuarioEmpresa(usuario.getId(), prestatariaId);
    if (!vinculado)
      throw new CustomException("No tiene acceso a las operaciones de esta empresa", HttpStatus.FORBIDDEN);
  }

  private OperacionListItemDto toListItem(OperacionEntity op) {
    long dias = op.getFechaCreacion() != null
        ? ChronoUnit.DAYS.between(op.getFechaCreacion(), LocalDate.now())
        : 0;
    return OperacionListItemDto.builder()
        .id(op.getId())
        .referencia(op.getReferencia())
        .empresaPrestamistaNombre(op.getEmpresaPrestamista().getRazonSocial())
        .empresaPrestatariaNombre(op.getEmpresaPrestataria().getRazonSocial())
        .cobraInteres(op.getCobraInteres())
        .montoEstimado(op.getMontoEstimado())
        .estadoPipeline(op.getEstadoPipeline())
        .fechaCreacion(op.getFechaCreacion())
        .creadoPor(op.getCreatedBy())
        .diasEsperando(dias)
        .build();
  }

  private OperacionResponseDto toResponseDto(OperacionEntity op,
                                              List<EventoPipelineDto> eventos,
                                              AvisoTramoAnteriorDto aviso) {
    String cuentaOrigenDesc = op.getCuentaOrigen() != null
        ? op.getCuentaOrigen().getBanco().getNombre() + " - " + op.getCuentaOrigen().getNumeroCuenta() : null;
    String cuentaDestinoDesc = op.getCuentaDestino() != null
        ? op.getCuentaDestino().getBanco().getNombre() + " - " + op.getCuentaDestino().getNumeroCuenta() : null;

    return OperacionResponseDto.builder()
        .id(op.getId())
        .referencia(op.getReferencia())
        .empresaPrestamistaId(op.getEmpresaPrestamista().getId())
        .empresaPrestamistaNombre(op.getEmpresaPrestamista().getRazonSocial())
        .empresaPrestamistaCodigoInterno(op.getEmpresaPrestamista().getCodigoInterno())
        .empresaPrestatariaId(op.getEmpresaPrestataria().getId())
        .empresaPrestatariaNombre(op.getEmpresaPrestataria().getRazonSocial())
        .empresaPrestatariaCodigoInterno(op.getEmpresaPrestataria().getCodigoInterno())
        .cobraInteres(op.getCobraInteres())
        .cuentaOrigenId(op.getCuentaOrigen() != null ? op.getCuentaOrigen().getId() : null)
        .cuentaOrigenDescripcion(cuentaOrigenDesc)
        .cuentaOrigenExentaGmf(op.getCuentaOrigen() != null ? op.getCuentaOrigen().getExentaGmf() : null)
        .cuentaDestinoId(op.getCuentaDestino() != null ? op.getCuentaDestino().getId() : null)
        .cuentaDestinoDescripcion(cuentaDestinoDesc)
        .montoEstimado(op.getMontoEstimado())
        .observaciones(op.getObservaciones())
        .numDocumentoSoporte(op.getNumDocumentoSoporte())
        .estadoPipeline(op.getEstadoPipeline())
        .fechaCreacion(op.getFechaCreacion())
        .creadoPor(op.getCreatedBy())
        .aprobacionInternaAt(op.getAprobacionInternaAt())
        .aprobacionInternaUsuario(op.getAprobacionInternaUsuario() != null
            ? op.getAprobacionInternaUsuario().getNombre() : null)
        .aprobacionInternaObservacion(op.getAprobacionInternaObservacion())
        .aceptacionEmpresaAt(op.getAceptacionEmpresaAt())
        .aceptacionEmpresaUsuario(op.getAceptacionEmpresaUsuario() != null
            ? op.getAceptacionEmpresaUsuario().getNombre() : null)
        .aceptacionEmpresaObservacion(op.getAceptacionEmpresaObservacion())
        .firmaDigitalAt(op.getFirmaDigitalAt())
        .desembolsoAt(op.getDesembolsoAt())
        .eventos(eventos)
        .avisoTramoAnterior(aviso)
        .build();
  }

  private EventoPipelineDto toEventoDto(EventoPipelineEntity e) {
    return EventoPipelineDto.builder()
        .estadoAnterior(e.getEstadoAnterior())
        .estadoNuevo(e.getEstadoNuevo())
        .usuario(e.getUsuario() != null ? e.getUsuario().getNombre() : "sistema")
        .observacion(e.getObservacion())
        .ocurridoAt(e.getOcurridoAt())
        .build();
  }
}
