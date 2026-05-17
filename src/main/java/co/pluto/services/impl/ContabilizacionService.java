package co.pluto.services.impl;

import co.pluto.dto.response.contabilizacion.AsientoContableDetalleDto;
import co.pluto.dto.response.contabilizacion.AsientoContableResponseDto;
import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.utils.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContabilizacionService {

  private static final String TIPO_LIQUIDACION       = "LIQUIDACION";
  private static final String TIPO_DESEMBOLSO        = "DESEMBOLSO";
  private static final String TIPO_LIQUIDACION_DIARIA = "LIQUIDACION_DIARIA";

  private final IAsientoContableRepository            asientoRepo;
  private final IInterfazContableRepository           interfazRepo;
  private final IInterfazContableLineaRepository      lineaInterfazRepo;
  private final IPucRepository                        pucRepo;
  private final IEmpresaRepository                    empresaRepo;
  private final ILiquidacionMensualDetalleRepository  detalleRepo;
  private final ILiquidacionDiariaDetalleRepository   detalleDiarioRepo;
  private final IOperacionRepository                  operacionRepo;
  private final ISaldoInicialRepository               saldoInicialRepo;
  private final ITipoMovimientoContableRepository     tipoMovRepo;

  // ── Liquidación mensual ─────────────────────────────────────────

  /**
   * Genera un asiento de CAUSACION_INTERESES por cada empresa con detalles
   * en la liquidación. Idempotente: no duplica si ya existe para esa empresa.
   */
  @Transactional
  public List<AsientoContableResponseDto> contabilizarLiquidacion(LiquidacionMensualEntity liq) {
    List<LiquidacionMensualDetalleEntity> detalles = detalleRepo.findAllByLiquidacionId(liq.getId());

    // Agrupar total de intereses por empresa prestamista
    Map<Long, BigDecimal> totalesPorEmpresa = calcularTotalesPorEmpresaLiquidacion(detalles);

    TipoMovimientoContableEntity tipoMov = tipoMovRepo.findByCodigoAndActivoIsTrue("CAUSACION_INTERESES")
        .orElseThrow(() -> new BusinessException("Tipo de movimiento CAUSACION_INTERESES no configurado"));

    List<AsientoContableResponseDto> resultado = new ArrayList<>();

    for (Map.Entry<Long, BigDecimal> entry : totalesPorEmpresa.entrySet()) {
      Long empresaId = entry.getKey();
      BigDecimal total = entry.getValue();

      if (asientoRepo.existsByTipoOrigenAndOrigenIdAndEmpresaId(TIPO_LIQUIDACION, liq.getId(), empresaId)) {
        // Ya contabilizado — incluir en respuesta
        asientoRepo.findAllByTipoOrigenAndOrigenIdOrderByIdAsc(TIPO_LIQUIDACION, liq.getId())
            .stream().filter(a -> a.getEmpresaId().equals(empresaId))
            .findFirst().ifPresent(a -> resultado.add(toDto(a)));
        continue;
      }

      InterfazContableEntity interfaz = interfazRepo
          .findByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(empresaId, tipoMov.getId())
          .orElse(null);

      if (interfaz == null) {
        log.warn("Sin interfaz CAUSACION_INTERESES para empresa {} — asiento omitido", empresaId);
        continue;
      }

      EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(empresaId).orElseThrow();
      AsientoContableEntity asiento = generarAsiento(
          TIPO_LIQUIDACION, liq.getId(), empresaId, interfaz,
          liq.getFechaCorte(),
          "Causación intereses " + liq.getMes() + "/" + liq.getAnio()
              + " — " + empresa.getRazonSocial(),
          total
      );
      resultado.add(toDto(asientoRepo.save(asiento)));
    }

    return resultado;
  }

  // ── Liquidación diaria ─────────────────────────────────────────

  /**
   * Genera un asiento de CAUSACION_INTERESES por cada empresa prestamista con detalles
   * en la liquidación diaria. Idempotente: no duplica si ya existe para esa empresa.
   */
  @Transactional
  public List<AsientoContableResponseDto> contabilizarLiquidacionDiaria(LiquidacionDiariaEntity liq) {
    List<LiquidacionDiariaDetalleEntity> detalles = detalleDiarioRepo.findAllByLiquidacionId(liq.getId());

    // Agrupar total de intereses por empresa prestamista
    Map<Long, BigDecimal> totalesPorEmpresa = calcularTotalesPorEmpresaDiaria(detalles);

    TipoMovimientoContableEntity tipoMov = tipoMovRepo.findByCodigoAndActivoIsTrue("CAUSACION_INTERESES")
        .orElseThrow(() -> new BusinessException("Tipo de movimiento CAUSACION_INTERESES no configurado"));

    List<AsientoContableResponseDto> resultado = new ArrayList<>();

    for (Map.Entry<Long, BigDecimal> entry : totalesPorEmpresa.entrySet()) {
      Long empresaId = entry.getKey();
      BigDecimal total = entry.getValue();

      if (asientoRepo.existsByTipoOrigenAndOrigenIdAndEmpresaId(TIPO_LIQUIDACION_DIARIA, liq.getId(), empresaId)) {
        asientoRepo.findAllByTipoOrigenAndOrigenIdOrderByIdAsc(TIPO_LIQUIDACION_DIARIA, liq.getId())
            .stream().filter(a -> a.getEmpresaId().equals(empresaId))
            .findFirst().ifPresent(a -> resultado.add(toDto(a)));
        continue;
      }

      InterfazContableEntity interfaz = interfazRepo
          .findByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(empresaId, tipoMov.getId())
          .orElse(null);

      if (interfaz == null) {
        log.warn("Sin interfaz CAUSACION_INTERESES para empresa {} — asiento diario omitido", empresaId);
        continue;
      }

      EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(empresaId).orElseThrow();
      AsientoContableEntity asiento = generarAsiento(
          TIPO_LIQUIDACION_DIARIA, liq.getId(), empresaId, interfaz,
          liq.getFecha(),
          "Causación intereses " + liq.getFecha() + " — " + empresa.getRazonSocial(),
          total
      );
      resultado.add(toDto(asientoRepo.save(asiento)));
    }

    return resultado;
  }

  // ── Desembolso ──────────────────────────────────────────────────

  /**
   * Genera el asiento de DESEMBOLSO para la empresa prestamista.
   * Llamado automáticamente al confirmar el desembolso.
   * Falla silenciosa si no hay interfaz configurada.
   */
  @Transactional
  public AsientoContableResponseDto contabilizarDesembolso(DesembolsoEntity desembolso,
                                                             OperacionEntity op) {
    Long empresaId = op.getEmpresaPrestamista().getId();

    if (asientoRepo.existsByTipoOrigenAndOrigenIdAndEmpresaId(TIPO_DESEMBOLSO, desembolso.getId(), empresaId)) {
      return asientoRepo.findAllByTipoOrigenAndOrigenIdOrderByIdAsc(TIPO_DESEMBOLSO, desembolso.getId())
          .stream().filter(a -> a.getEmpresaId().equals(empresaId))
          .findFirst().map(this::toDto).orElse(null);
    }

    TipoMovimientoContableEntity tipoMov = tipoMovRepo.findByCodigoAndActivoIsTrue("DESEMBOLSO").orElse(null);
    if (tipoMov == null) return null;

    InterfazContableEntity interfaz = interfazRepo
        .findByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(empresaId, tipoMov.getId())
        .orElse(null);

    if (interfaz == null) {
      log.warn("Sin interfaz DESEMBOLSO para empresa {} — asiento no generado", empresaId);
      return null;
    }

    AsientoContableEntity asiento = generarAsiento(
        TIPO_DESEMBOLSO, desembolso.getId(), empresaId, interfaz,
        desembolso.getFecha(),
        "Desembolso " + op.getReferencia() + " — " + op.getEmpresaPrestataria().getRazonSocial(),
        desembolso.getMonto()
    );
    return toDto(asientoRepo.save(asiento));
  }

  // ── Consulta ────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<AsientoContableResponseDto> listarPorOrigen(String tipoOrigen, Long origenId) {
    return asientoRepo.findAllByTipoOrigenAndOrigenIdOrderByIdAsc(tipoOrigen, origenId)
        .stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<AsientoContableResponseDto> buscar(String tipoOrigen, Long empresaId,
                                                  String estado,
                                                  LocalDate fechaDesde,
                                                  LocalDate fechaHasta) {
    Specification<AsientoContableEntity> spec = Specification.where(null);
    if (tipoOrigen != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("tipoOrigen"), tipoOrigen));
    if (empresaId  != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("empresaId"),  empresaId));
    if (estado     != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("estado"),     estado));
    if (fechaDesde != null) spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("fecha"), fechaDesde));
    if (fechaHasta != null) spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("fecha"),   fechaHasta));

    Sort sort = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
    return asientoRepo.findAll(spec, sort).stream().map(this::toDto).toList();
  }

  // ── helpers privados ────────────────────────────────────────────

  private AsientoContableEntity generarAsiento(String tipoOrigen, Long origenId,
                                                Long empresaId, InterfazContableEntity interfaz,
                                                LocalDate fecha, String descripcion,
                                                BigDecimal monto) {
    List<InterfazContableLineaEntity> lineasInterfaz =
        lineaInterfazRepo.findAllByInterfazIdOrderByOrdenAsc(interfaz.getId());

    AsientoContableEntity asiento = AsientoContableEntity.builder()
        .tipoOrigen(tipoOrigen)
        .origenId(origenId)
        .empresaId(empresaId)
        .interfazId(interfaz.getId())
        .fecha(fecha)
        .descripcion(descripcion)
        .estado("GENERADO")
        .build();

    List<AsientoContableDetalleEntity> detalle = lineasInterfaz.stream()
        .map(l -> AsientoContableDetalleEntity.builder()
            .asiento(asiento)
            .orden(l.getOrden())
            .cuentaPucId(l.getCuentaPucId())
            .naturaleza(l.getNaturaleza())
            .monto(monto)
            .glosa(l.getDescripcionGlosa())
            .build())
        .collect(Collectors.toList());

    asiento.getLineas().addAll(detalle);
    return asiento;
  }

  private Map<Long, BigDecimal> calcularTotalesPorEmpresaLiquidacion(
      List<LiquidacionMensualDetalleEntity> detalles) {

    Map<Long, BigDecimal> totales = new LinkedHashMap<>();

    for (LiquidacionMensualDetalleEntity d : detalles) {
      Long empresaId = null;
      if (d.getOperacionId() != null) {
        empresaId = operacionRepo.findByIdAndDeletedAtIsNull(d.getOperacionId())
            .map(op -> op.getEmpresaPrestamista().getId()).orElse(null);
      } else if (d.getSaldoInicialId() != null) {
        empresaId = saldoInicialRepo.findByIdAndDeletedAtIsNull(d.getSaldoInicialId())
            .map(si -> si.getEmpresaPrestamista().getId()).orElse(null);
      }
      if (empresaId != null) {
        totales.merge(empresaId, d.getInteresesPeriodo(), BigDecimal::add);
      }
    }
    return totales;
  }

  private Map<Long, BigDecimal> calcularTotalesPorEmpresaDiaria(
      List<LiquidacionDiariaDetalleEntity> detalles) {

    Map<Long, BigDecimal> totales = new LinkedHashMap<>();

    for (LiquidacionDiariaDetalleEntity d : detalles) {
      Long empresaId = null;
      if (d.getOperacionId() != null) {
        empresaId = operacionRepo.findByIdAndDeletedAtIsNull(d.getOperacionId())
            .map(op -> op.getEmpresaPrestamista().getId()).orElse(null);
      } else if (d.getSaldoInicialId() != null) {
        empresaId = saldoInicialRepo.findByIdAndDeletedAtIsNull(d.getSaldoInicialId())
            .map(si -> si.getEmpresaPrestamista().getId()).orElse(null);
      }
      if (empresaId != null) {
        totales.merge(empresaId, d.getInteresesPeriodo(), BigDecimal::add);
      }
    }
    return totales;
  }

  private AsientoContableResponseDto toDto(AsientoContableEntity a) {
    String empresaNombre = empresaRepo.findByIdAndDeletedAtIsNull(a.getEmpresaId())
        .map(EmpresaEntity::getRazonSocial).orElse(null);
    String interfazNombre = interfazRepo.findByIdAndDeletedAtIsNull(a.getInterfazId())
        .map(InterfazContableEntity::getNombre).orElse(null);

    List<AsientoContableDetalleDto> lineas = a.getLineas().stream()
        .map(l -> {
          PucEntity puc = pucRepo.findByIdAndDeletedAtIsNull(l.getCuentaPucId()).orElse(null);
          return AsientoContableDetalleDto.builder()
              .orden(l.getOrden())
              .cuentaPucId(l.getCuentaPucId())
              .cuentaCodigo(puc != null ? puc.getCodigo() : null)
              .cuentaNombre(puc != null ? puc.getNombre() : null)
              .naturaleza(l.getNaturaleza())
              .monto(l.getMonto())
              .glosa(l.getGlosa())
              .build();
        }).toList();

    return AsientoContableResponseDto.builder()
        .id(a.getId())
        .tipoOrigen(a.getTipoOrigen())
        .origenId(a.getOrigenId())
        .empresaId(a.getEmpresaId())
        .empresaNombre(empresaNombre)
        .interfazId(a.getInterfazId())
        .interfazNombre(interfazNombre)
        .fecha(a.getFecha())
        .descripcion(a.getDescripcion())
        .estado(a.getEstado())
        .createdAt(a.getCreatedAt())
        .createdBy(a.getCreatedBy())
        .lineas(lineas)
        .build();
  }
}
