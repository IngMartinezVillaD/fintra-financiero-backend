package co.pluto.services.impl;

import co.pluto.dto.request.desembolso.ConfirmarDesembolsoRequestDto;
import co.pluto.dto.request.desembolso.GenerarArchivoPlanoRequestDto;
import co.pluto.dto.response.desembolso.*;
import co.pluto.dto.response.operaciones.OperacionListItemDto;
import co.pluto.dto.response.tasas.TasaPeriodoResponseDto;
import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.services.impl.bancos.ArchivoPlanoGenerator;
import co.pluto.services.impl.bancos.ArchivoPlanoGeneratorRegistry;
import co.pluto.services.impl.bancos.ArchivoPlanoResult;
import co.pluto.services.interfaces.IDesembolsoService;
import co.pluto.services.interfaces.ITasaPeriodoService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DesembolsoServiceImpl implements IDesembolsoService {

  private static final BigDecimal TARIFA_GMF = new BigDecimal("0.004"); // 4/1000

  private final IOperacionRepository    operacionRepo;
  private final ICupoRotativoRepository cupoRepo;
  private final IDesembolsoRepository   desembolsoRepo;
  private final IGmfMovimientoRepository gmfRepo;
  private final IArchivoPlanoRepository  archivoPlanoRepo;
  private final ITramoRepository         tramoRepo;
  private final IThomasSigneSolicitudRepository solicitudRepo;
  private final ITasaEspecialEmpresaRepository tasaEspecialRepo;
  private final ITasaPeriodoService      tasaPeriodoService;
  private final ArchivoPlanoGeneratorRegistry generatorRegistry;
  private final IEventoPipelineRepository eventoRepo;
  private final IUsuarioRepository       usuarioRepo;
  private final ContabilizacionService   contabilizacionService;

  // ── Bandeja pendientes ───────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<OperacionListItemDto> listarPendientes() {
    return operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("FD")
        .stream()
        .filter(op -> tienesFirmaCompletada(op.getId()))
        .map(this::toListItem)
        .collect(Collectors.toList());
  }

  // ── Preview GMF ─────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public GmfResumenDto calcularGmfPreview(Long operacionId, BigDecimal monto) {
    OperacionEntity op = findOrThrow(operacionId);
    return calcularGmf(op, monto);
  }

  // ── Confirmar desembolso ─────────────────────────────────────────

  @Override
  public DesembolsoResponseDto confirmar(Long operacionId, ConfirmarDesembolsoRequestDto req) {
    OperacionEntity op = findOrThrow(operacionId);

    if (!"FD".equals(op.getEstadoPipeline()))
      throw new BusinessException("Solo se puede desembolsar una operación en estado FD");

    if (!tienesFirmaCompletada(operacionId))
      throw new BusinessException("La operación no tiene firma digital completada (FIRMADA)");

    // Idempotencia: si ya fue desembolsada, devolver el desembolso existente
    if (desembolsoRepo.existsByOperacionId(operacionId)) {
      DesembolsoEntity existente = desembolsoRepo.findByOperacionId(operacionId).orElseThrow();
      TramoEntity tramo = tramoRepo
          .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(operacionId, "EN_CURSO")
          .orElse(null);
      return toDto(existente, op.getReferencia(), tramo);
    }

    LocalDate fecha = req.getFecha() != null ? req.getFecha() : LocalDate.now();

    // 1. Calcular GMF
    GmfResumenDto gmf = calcularGmf(op, req.getMonto());

    // 2. Persistir desembolso
    DesembolsoEntity desembolso = DesembolsoEntity.builder()
        .operacionId(operacionId)
        .monto(req.getMonto())
        .fecha(fecha)
        .gmfAplica(gmf.getAplica())
        .gmfCalculado(gmf.getMonto())
        .build();
    desembolso = desembolsoRepo.save(desembolso);

    // 3. Registrar movimiento GMF extracontable si aplica
    if (Boolean.TRUE.equals(gmf.getAplica())) {
      gmfRepo.save(GmfMovimientoEntity.builder()
          .empresaId(op.getEmpresaPrestamista().getId())
          .operacionId(operacionId)
          .anio((short) fecha.getYear())
          .mes((short) fecha.getMonthValue())
          .montoGmf(gmf.getMonto())
          .fecha(fecha)
          .decisionAnual("PENDIENTE")
          .build());
    }

    // 4. Abrir tramo inicial
    TramoEntity tramo = abrirTramoInicial(op, req.getMonto(), fecha);

    // 5. Descontar del cupo rotativo
    if (op.getCupoRotativo() != null) {
      CupoRotativoEntity cupo = op.getCupoRotativo();
      BigDecimal nuevoSaldo = cupo.getSaldoDisponible().subtract(req.getMonto());
      if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0)
        throw new BusinessException("El monto desembolsado supera el saldo disponible del cupo "
            + cupo.getCodigo());
      cupo.setSaldoDisponible(nuevoSaldo);
      cupoRepo.save(cupo);
    }

    // 6. Transicionar operación FD → DS
    op.setEstadoPipeline("DS");
    op.setDesembolsoAt(OffsetDateTime.now());
    operacionRepo.save(op);

    // 7. Registrar evento pipeline
    registrarEvento(op, "FD", "DS", "Desembolso confirmado. Monto: " + req.getMonto());

    // 8. Generar asiento contable (falla silenciosa si no hay interfaz configurada)
    try {
      contabilizacionService.contabilizarDesembolso(desembolso, op);
    } catch (Exception e) {
      log.warn("No se pudo generar asiento contable para desembolso {}: {}", op.getReferencia(), e.getMessage());
    }

    log.info("Desembolso confirmado para operación {} → monto={} GMF={}", op.getReferencia(), req.getMonto(), gmf.getMonto());

    return toDto(desembolso, op.getReferencia(), tramo);
  }

  // ── Lista desembolsos por operación ─────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<DesembolsoResponseDto> listarPorOperacion(Long operacionId) {
    findOrThrow(operacionId);
    return desembolsoRepo.findAllByOperacionId(operacionId)
        .stream()
        .map(d -> toDto(d, null, null))
        .collect(Collectors.toList());
  }

  // ── Archivo plano ────────────────────────────────────────────────

  @Override
  public List<ArchivoPlanoResponseDto> generarArchivoPlano(GenerarArchivoPlanoRequestDto req) {
    List<OperacionEntity> operaciones = req.getOperacionIds().stream()
        .map(this::findOrThrow)
        .collect(Collectors.toList());

    for (OperacionEntity op : operaciones) {
      if (!"FD".equals(op.getEstadoPipeline()))
        throw new BusinessException("La operación " + op.getReferencia() + " no está en estado FD");
      if (!tienesFirmaCompletada(op.getId()))
        throw new BusinessException("La operación " + op.getReferencia() + " no tiene firma completada");
    }

    // Agrupar por banco de la cuenta origen
    Map<String, List<OperacionEntity>> porBanco = operaciones.stream()
        .filter(op -> op.getCuentaOrigen() != null && op.getCuentaOrigen().getBanco() != null)
        .collect(Collectors.groupingBy(op -> op.getCuentaOrigen().getBanco().getCodigo()));

    List<ArchivoPlanoResponseDto> resultado = new ArrayList<>();

    for (Map.Entry<String, List<OperacionEntity>> entry : porBanco.entrySet()) {
      String bancoCodigo = entry.getKey();
      List<OperacionEntity> opsDelBanco = entry.getValue();

      ArchivoPlanoGenerator gen = generatorRegistry.getForBanco(bancoCodigo);

      // Para la generación del archivo usamos el monto estimado (aún no está confirmado el desembolso)
      List<DesembolsoEntity> desembolsosExistentes = opsDelBanco.stream()
          .flatMap(op -> desembolsoRepo.findAllByOperacionId(op.getId()).stream())
          .collect(Collectors.toList());

      ArchivoPlanoResult archivoResult = gen.generar(opsDelBanco, desembolsosExistentes, req.getFechaDesembolso());

      ArchivoPlanoEntity archivo = ArchivoPlanoEntity.builder()
          .bancoCodigo(bancoCodigo)
          .formato(gen.formato())
          .contenido(archivoResult.getContenido())
          .totalRegistros(archivoResult.getTotalRegistros())
          .totalMonto(archivoResult.getTotalMonto())
          .fechaGeneracion(req.getFechaDesembolso())
          .build();

      archivo = archivoPlanoRepo.save(archivo);

      String bancoNombre = opsDelBanco.get(0).getCuentaOrigen().getBanco().getNombre();

      resultado.add(ArchivoPlanoResponseDto.builder()
          .id(archivo.getId())
          .bancoCodigo(bancoCodigo)
          .bancoNombre(bancoNombre)
          .formato(gen.formato())
          .totalRegistros(archivoResult.getTotalRegistros())
          .totalMonto(archivoResult.getTotalMonto())
          .fechaGeneracion(req.getFechaDesembolso())
          .operacionIds(opsDelBanco.stream().map(OperacionEntity::getId).collect(Collectors.toList()))
          .urlDescarga("/api/v1/desembolsos/archivos-planos/" + archivo.getId() + "/descargar")
          .build());
    }

    return resultado;
  }

  @Override
  @Transactional(readOnly = true)
  public String obtenerContenidoArchivoPlano(Long archivoPlanoId) {
    return archivoPlanoRepo.findById(archivoPlanoId)
        .orElseThrow(() -> new CustomException("Archivo plano no encontrado", HttpStatus.NOT_FOUND))
        .getContenido();
  }

  // ── Helpers ──────────────────────────────────────────────────────

  private GmfResumenDto calcularGmf(OperacionEntity op, BigDecimal monto) {
    boolean exenta = op.getCuentaOrigen() != null && Boolean.TRUE.equals(op.getCuentaOrigen().getExentaGmf());
    if (exenta) {
      return GmfResumenDto.builder()
          .aplica(false)
          .monto(BigDecimal.ZERO)
          .tarifa(TARIFA_GMF)
          .motivoExencion("Cuenta origen exenta de GMF")
          .build();
    }
    BigDecimal gmf = monto.multiply(TARIFA_GMF).setScale(6, RoundingMode.HALF_EVEN);
    return GmfResumenDto.builder()
        .aplica(true)
        .monto(gmf)
        .tarifa(TARIFA_GMF)
        .motivoExencion(null)
        .build();
  }

  private TramoEntity abrirTramoInicial(OperacionEntity op, BigDecimal monto, LocalDate fechaDesde) {
    YearMonth mes = YearMonth.from(fechaDesde);
    LocalDate fechaHasta = mes.atEndOfMonth();
    int dias = Math.max(1, (int) ChronoUnit.DAYS.between(fechaDesde, fechaHasta));

    String cobraInteres = op.getCobraInteres();
    BigDecimal tasaMensual;
    String tipoTasa;

    if ("SI_COMERCIAL".equals(cobraInteres)) {
      tasaMensual = tasaPeriodoService.listarVigentesHoy().stream()
          .filter(t -> "COMERCIAL_VIGENTE".equals(t.getTipoTasa()))
          .map(TasaPeriodoResponseDto::getValorPorcentajeMensual)
          .findFirst()
          .orElse(BigDecimal.ZERO);
      tipoTasa = "COMERCIAL";
    } else if ("SI_ESPECIAL".equals(cobraInteres)) {
      tasaMensual = tasaEspecialRepo
          .findFirstByEmpresaIdAndEstadoAndDeletedAtIsNull(op.getEmpresaPrestataria().getId(), "VIGENTE")
          .map(TasaEspecialEmpresaEntity::getValorPorcentajeMensual)
          .orElse(BigDecimal.ZERO);
      tipoTasa = "ESPECIAL";
    } else {
      tasaMensual = BigDecimal.ZERO;
      tipoTasa = "SIN_INTERES";
    }

    BigDecimal interes = monto
        .multiply(tasaMensual.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN))
        .multiply(BigDecimal.valueOf(dias).divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_EVEN))
        .setScale(6, RoundingMode.HALF_EVEN);

    TramoEntity tramo = TramoEntity.builder()
        .operacion(op)
        .numeroTramo(1)
        .tipoMovimiento("DESEMBOLSO_INICIAL")
        .fechaDesde(fechaDesde)
        .fechaHasta(fechaHasta)
        .saldoCapital(monto)
        .dias(dias)
        .tasaPorcentajeMensual(tasaMensual)
        .tipoTasa(tipoTasa)
        .interesCalculado(interes)
        .estado("EN_CURSO")
        .build();

    return tramoRepo.save(tramo);
  }

  private boolean tienesFirmaCompletada(Long operacionId) {
    return solicitudRepo.findByIdempotencyKey("FIRMA-" + operacionId)
        .map(s -> "FIRMADA".equals(s.getEstado()))
        .orElse(false);
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
        .observacion(obs)
        .ocurridoAt(OffsetDateTime.now())
        .build());
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

  private DesembolsoResponseDto toDto(DesembolsoEntity d, String referencia, TramoEntity tramo) {
    TramoInicialDto tramoDto = null;
    if (tramo != null) {
      tramoDto = TramoInicialDto.builder()
          .id(tramo.getId())
          .numeroTramo(tramo.getNumeroTramo())
          .fechaDesde(tramo.getFechaDesde())
          .fechaHasta(tramo.getFechaHasta())
          .dias(tramo.getDias())
          .saldoCapital(tramo.getSaldoCapital())
          .tasaPorcentajeMensual(tramo.getTasaPorcentajeMensual())
          .tipoTasa(tramo.getTipoTasa())
          .interesCalculado(tramo.getInteresCalculado())
          .build();
    }
    return DesembolsoResponseDto.builder()
        .id(d.getId())
        .operacionId(d.getOperacionId())
        .referencia(referencia)
        .monto(d.getMonto())
        .fecha(d.getFecha())
        .gmfAplica(d.getGmfAplica())
        .gmfCalculado(d.getGmfCalculado())
        .archivoPlanoId(d.getArchivoPlanoId())
        .createdAt(d.getCreatedAt())
        .tramoInicial(tramoDto)
        .build();
  }
}
