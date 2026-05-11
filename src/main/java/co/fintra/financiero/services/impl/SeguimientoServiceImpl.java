package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.request.abono.RegistrarAbonoRequestDto;
import co.fintra.financiero.dto.response.operaciones.OperacionListItemDto;
import co.fintra.financiero.dto.response.seguimiento.*;
import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.services.impl.seguimiento.AplicacionAbono;
import co.fintra.financiero.services.impl.seguimiento.MotorTramosService;
import co.fintra.financiero.services.impl.seguimiento.ResolutorTasaAplicableService;
import co.fintra.financiero.services.impl.seguimiento.TasaAplicable;
import co.fintra.financiero.services.interfaces.ISeguimientoService;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SeguimientoServiceImpl implements ISeguimientoService {

  private final IOperacionRepository    operacionRepo;
  private final ITramoRepository        tramoRepo;
  private final IAbonoRepository        abonoRepo;
  private final IDesembolsoRepository   desembolsoRepo;
  private final IGmfMovimientoRepository gmfRepo;
  private final IEventoPipelineRepository eventoRepo;
  private final IUsuarioRepository      usuarioRepo;
  private final MotorTramosService      motor;
  private final ResolutorTasaAplicableService resolutor;

  // ── Lista operaciones vigentes (DS) ─────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<OperacionListItemDto> listarVigentes() {
    return operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS")
        .stream().map(this::toListItem).collect(Collectors.toList());
  }

  // ── Snapshot completo ────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public SeguimientoOperacionResponseDto obtenerSeguimiento(Long operacionId) {
    OperacionEntity op = findOrThrow(operacionId);
    var desembolso = desembolsoRepo.findByOperacionId(operacionId).orElse(null);
    var tramos  = listarTramos(operacionId);
    var abonos  = listarAbonos(operacionId);
    var saldos  = calcularSaldos(operacionId, op);

    return SeguimientoOperacionResponseDto.builder()
        .id(op.getId())
        .referencia(op.getReferencia())
        .empresaPrestamistaNombre(op.getEmpresaPrestamista().getRazonSocial())
        .empresaPrestatariaNombre(op.getEmpresaPrestataria().getRazonSocial())
        .cobraInteres(op.getCobraInteres())
        .fechaDesembolso(desembolso != null ? desembolso.getFecha() : null)
        .montoDesembolsado(desembolso != null ? desembolso.getMonto() : null)
        .saldos(saldos)
        .tramos(tramos)
        .abonos(abonos)
        .build();
  }

  // ── Saldos separados ─────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public SaldosSeparadosDto obtenerSaldos(Long operacionId) {
    return calcularSaldos(operacionId, findOrThrow(operacionId));
  }

  // ── Tramos ───────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<TramoDto> listarTramos(Long operacionId) {
    return tramoRepo.findAllByOperacionIdAndDeletedAtIsNullOrderByFechaDesdeAsc(operacionId)
        .stream().map(this::toTramoDto).collect(Collectors.toList());
  }

  // ── Abonos ───────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<AbonoDto> listarAbonos(Long operacionId) {
    return abonoRepo.findAllByOperacionIdOrderByFechaAsc(operacionId)
        .stream().map(this::toAbonoDto).collect(Collectors.toList());
  }

  // ── Preview (sin persistir) ──────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public RegistrarAbonoResponseDto previewAbono(Long operacionId, RegistrarAbonoRequestDto req) {
    OperacionEntity op = findOrThrow(operacionId);
    validarEstadoDS(op);

    BigDecimal saldoCapital    = calcularSaldoCapital(operacionId);
    BigDecimal interesCausado  = calcularInteresCausado(operacionId);
    AplicacionAbono aplicacion = motor.aplicarAbono(interesCausado, saldoCapital, req.getMonto());

    TasaAplicable tasa = resolutor.resolver(op, req.getFechaAbono());
    int nuevoNumero = tramoRepo.maxNumeroTramo(operacionId) + 1;

    TramoDto nuevoTramoPreview = null;
    if (aplicacion.nuevoSaldoCapital().compareTo(BigDecimal.ZERO) > 0) {
      TramoEntity nuevoTramo = motor.abrirNuevoTramo(op, nuevoNumero,
          req.getFechaAbono(), aplicacion.nuevoSaldoCapital(), tasa, "LIQUIDACION_POR_ABONO");
      nuevoTramoPreview = toTramoDto(nuevoTramo);
    }

    SaldosSeparadosDto saldosPost = calcularSaldosConAplicacion(
        operacionId, op, aplicacion.nuevoSaldoCapital(), aplicacion.interesesPendientes());

    AbonoDto abonoPreview = AbonoDto.builder()
        .fecha(req.getFechaAbono())
        .montoTotal(req.getMonto())
        .aplicadoAIntereses(aplicacion.aplicadoAIntereses())
        .aplicadoACapital(aplicacion.aplicadoACapital())
        .numeroComprobante(req.getNumeroComprobante())
        .observaciones(req.getObservaciones())
        .build();

    return RegistrarAbonoResponseDto.builder()
        .abono(abonoPreview)
        .tramoNuevo(nuevoTramoPreview)
        .saldosActuales(saldosPost)
        .operacionSaldada(aplicacion.nuevoSaldoCapital().compareTo(BigDecimal.ZERO) == 0)
        .build();
  }

  // ── Registrar abono (persiste) ───────────────────────────────────

  @Override
  public RegistrarAbonoResponseDto registrarAbono(Long operacionId, RegistrarAbonoRequestDto req) {
    OperacionEntity op = findOrThrow(operacionId);
    validarEstadoDS(op);

    // Idempotencia por comprobante
    if (abonoRepo.existsByOperacionIdAndNumeroComprobante(operacionId, req.getNumeroComprobante())) {
      AbonoEntity existente = abonoRepo
          .findByOperacionIdAndNumeroComprobante(operacionId, req.getNumeroComprobante()).orElseThrow();
      TramoEntity tramoPost = tramoRepo
          .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(operacionId, "EN_CURSO")
          .orElse(null);
      return RegistrarAbonoResponseDto.builder()
          .abono(toAbonoDto(existente))
          .tramoNuevo(tramoPost != null ? toTramoDto(tramoPost) : null)
          .saldosActuales(calcularSaldos(operacionId, op))
          .operacionSaldada(calcularSaldoCapital(operacionId).compareTo(BigDecimal.ZERO) == 0)
          .build();
    }

    BigDecimal saldoCapital   = calcularSaldoCapital(operacionId);
    BigDecimal interesCausado = calcularInteresCausado(operacionId);

    // 1. Aplicar abono (validación incluida en motor)
    AplicacionAbono aplicacion = motor.aplicarAbono(interesCausado, saldoCapital, req.getMonto());

    // 2. Cerrar tramo en curso
    TramoEntity tramoActual = tramoRepo
        .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(operacionId, "EN_CURSO")
        .orElseThrow(() -> new BusinessException("No hay tramo en curso para esta operación"));

    motor.cerrarTramoEnCurso(tramoActual, req.getFechaAbono(), "LIQUIDACION_POR_ABONO");
    TramoEntity tramoCerrado = tramoRepo.save(tramoActual);

    // 3. Persistir abono
    UsuarioEntity usuario = currentUser();
    AbonoEntity abono = AbonoEntity.builder()
        .operacionId(operacionId)
        .fecha(req.getFechaAbono())
        .montoTotal(req.getMonto())
        .numeroComprobante(req.getNumeroComprobante())
        .aplicadoAIntereses(aplicacion.aplicadoAIntereses())
        .aplicadoACapital(aplicacion.aplicadoACapital())
        .tramoLiquidadoId(tramoCerrado.getId())
        .usuarioId(usuario != null ? usuario.getId() : 1L)
        .observaciones(req.getObservaciones())
        .build();
    abono = abonoRepo.save(abono);

    // 4. Abrir nuevo tramo o saldar operación
    TramoEntity tramoNuevo = null;
    boolean saldada = aplicacion.nuevoSaldoCapital().compareTo(BigDecimal.ZERO) == 0;

    if (!saldada) {
      TasaAplicable tasa = resolutor.resolver(op, req.getFechaAbono());
      int nuevoNumero = tramoRepo.maxNumeroTramo(operacionId) + 1;
      tramoNuevo = motor.abrirNuevoTramo(op, nuevoNumero,
          req.getFechaAbono(), aplicacion.nuevoSaldoCapital(), tasa, "LIQUIDACION_POR_ABONO");
      tramoNuevo = tramoRepo.save(tramoNuevo);
    } else {
      registrarEvento(op, "DS", "DS", "Operación saldada — capital pagado en su totalidad");
      log.info("Operación {} saldada con abono {}", op.getReferencia(), req.getNumeroComprobante());
    }

    registrarEvento(op, null, "DS",
        String.format("Abono %s registrado. Capital: %.2f, Intereses: %.2f",
            req.getNumeroComprobante(), aplicacion.aplicadoACapital(), aplicacion.aplicadoAIntereses()));

    SaldosSeparadosDto saldosPost = calcularSaldos(operacionId, op);

    return RegistrarAbonoResponseDto.builder()
        .abono(toAbonoDto(abono))
        .tramoNuevo(tramoNuevo != null ? toTramoDto(tramoNuevo) : null)
        .saldosActuales(saldosPost)
        .operacionSaldada(saldada)
        .build();
  }

  // ── Cálculos internos ────────────────────────────────────────────

  private BigDecimal calcularSaldoCapital(Long operacionId) {
    BigDecimal desembolso = desembolsoRepo.findByOperacionId(operacionId)
        .map(DesembolsoEntity::getMonto).orElse(BigDecimal.ZERO);
    BigDecimal abonos = abonoRepo.sumAplicadoACapital(operacionId);
    return desembolso.subtract(abonos);
  }

  private BigDecimal calcularInteresCausado(Long operacionId) {
    BigDecimal interesTramos = tramoRepo.sumInteresLiquidado(operacionId);
    BigDecimal interesAplicado = abonoRepo.sumAplicadoAIntereses(operacionId);
    return interesTramos.subtract(interesAplicado).max(BigDecimal.ZERO);
  }

  private BigDecimal calcularGmfIncurrido(Long operacionId) {
    return gmfRepo.findAllByOperacionIdOrderByFechaAsc(operacionId)
        .stream().map(GmfMovimientoEntity::getMontoGmf)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private SaldosSeparadosDto calcularSaldos(Long operacionId, OperacionEntity op) {
    BigDecimal saldoCapital   = calcularSaldoCapital(operacionId);
    BigDecimal interesCausado = calcularInteresCausado(operacionId);
    BigDecimal gmfIncurrido   = calcularGmfIncurrido(operacionId);

    TramoEntity tramoActivo = tramoRepo
        .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(operacionId, "EN_CURSO")
        .orElse(null);
    BigDecimal interesEnCurso = motor.calcularInteresEnCurso(tramoActivo, LocalDate.now());

    return SaldosSeparadosDto.builder()
        .saldoCapital(saldoCapital)
        .interesesCausados(interesCausado)
        .interesEnCurso(interesEnCurso)
        .gmfIncurrido(gmfIncurrido)
        .deudaTotal(saldoCapital.add(interesCausado))
        .build();
  }

  private SaldosSeparadosDto calcularSaldosConAplicacion(Long operacionId, OperacionEntity op,
                                                          BigDecimal nuevoCapital,
                                                          BigDecimal interesesPendientes) {
    BigDecimal gmfIncurrido = calcularGmfIncurrido(operacionId);
    return SaldosSeparadosDto.builder()
        .saldoCapital(nuevoCapital)
        .interesesCausados(interesesPendientes)
        .interesEnCurso(BigDecimal.ZERO)
        .gmfIncurrido(gmfIncurrido)
        .deudaTotal(nuevoCapital.add(interesesPendientes))
        .build();
  }

  // ── Helpers ──────────────────────────────────────────────────────

  private void validarEstadoDS(OperacionEntity op) {
    if (!"DS".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se pueden registrar abonos en operaciones con estado DS");
  }

  private OperacionEntity findOrThrow(Long id) {
    return operacionRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Operación no encontrada: " + id, HttpStatus.NOT_FOUND));
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

  private OperacionListItemDto toListItem(OperacionEntity op) {
    long dias = op.getFechaCreacion() != null
        ? ChronoUnit.DAYS.between(op.getFechaCreacion(), LocalDate.now()) : 0;
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

  private TramoDto toTramoDto(TramoEntity t) {
    return TramoDto.builder()
        .id(t.getId())
        .numeroTramo(t.getNumeroTramo())
        .tipoMovimiento(t.getTipoMovimiento())
        .fechaDesde(t.getFechaDesde())
        .fechaHasta(t.getFechaHasta())
        .dias(t.getDias())
        .saldoCapital(t.getSaldoCapital())
        .tasaPorcentajeMensual(t.getTasaPorcentajeMensual())
        .tipoTasa(t.getTipoTasa())
        .interesCalculado(t.getInteresCalculado())
        .estado(t.getEstado())
        .build();
  }

  private AbonoDto toAbonoDto(AbonoEntity a) {
    return AbonoDto.builder()
        .id(a.getId())
        .fecha(a.getFecha())
        .montoTotal(a.getMontoTotal())
        .aplicadoAIntereses(a.getAplicadoAIntereses())
        .aplicadoACapital(a.getAplicadoACapital())
        .numeroComprobante(a.getNumeroComprobante())
        .observaciones(a.getObservaciones())
        .tramoLiquidadoId(a.getTramoLiquidadoId())
        .createdAt(a.getCreatedAt())
        .build();
  }
}
