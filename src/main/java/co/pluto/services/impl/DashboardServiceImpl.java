package co.pluto.services.impl;

import co.pluto.dto.response.dashboard.*;
import co.pluto.models.repositories.ILiquidacionMensualDetalleRepository;
import co.pluto.models.repositories.ILiquidacionMensualRepository;
import co.pluto.models.repositories.IOperacionRepository;
import co.pluto.services.interfaces.IDashboardService;
import co.pluto.services.interfaces.ITasaPeriodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

  private static final String[] MESES_ES = {
      "", "ene","feb","mar","abr","may","jun",
      "jul","ago","sep","oct","nov","dic"
  };

  private final JdbcTemplate                    jdbc;
  private final IOperacionRepository            operacionRepo;
  private final ITasaPeriodoService             tasaPeriodoService;

  // ── Consolidado legacy (mantiene compatibilidad con módulo 09) ──

  @Override
  @Cacheable("empresasActivas")
  public List<ConsolidadoEmpresaDto> consolidadoFinanciero() {
    return jdbc.query("""
        SELECT empresa_id, codigo_interno, razon_social, nit,
               total_operaciones, saldo_capital_vigente,
               intereses_causados_pendientes, total_desembolsado, ultimo_desembolso_fecha
        FROM prestamos.vw_consolidado_financiero ORDER BY razon_social
        """,
        (rs, rowNum) -> ConsolidadoEmpresaDto.builder()
            .empresaId(rs.getLong("empresa_id"))
            .codigoInterno(rs.getString("codigo_interno"))
            .razonSocial(rs.getString("razon_social"))
            .nit(rs.getString("nit"))
            .totalOperaciones(rs.getLong("total_operaciones"))
            .saldoCapitalVigente(rs.getBigDecimal("saldo_capital_vigente"))
            .interesesCausadosPendientes(rs.getBigDecimal("intereses_causados_pendientes"))
            .totalDesembolsado(rs.getBigDecimal("total_desembolsado"))
            .ultimoDesembolsoFecha(rs.getObject("ultimo_desembolso_fecha", java.time.LocalDate.class))
            .build());
  }

  // ── Dashboard completo ───────────────────────────────────────────

  @Override
  @Cacheable("tasasVigentes")
  public DashboardResponseDto dashboard() {
    return DashboardResponseDto.builder()
        .pipeline(pipeline())
        .consolidado(consolidado())
        .alertas(alertas())
        .tasasVigentes(tasasVigentes())
        .build();
  }

  // ── Evolución mensual ────────────────────────────────────────────

  @Override
  public List<EvolucionMensualDto> evolucionMensual(LocalDate desde, LocalDate hasta, Long empresaId) {
    String filtroEmpresa = empresaId != null
        ? "AND (o.empresa_prestamista_id = " + empresaId + " OR o.empresa_prestataria_id = " + empresaId + ")"
        : "";

    String sql = """
        SELECT
          CAST(EXTRACT(YEAR  FROM d.fecha) AS smallint) AS anio,
          CAST(EXTRACT(MONTH FROM d.fecha) AS smallint) AS mes,
          COALESCE(SUM(t_lat.saldo_capital), 0)         AS saldo_capital,
          COALESCE(SUM(lmd_lat.total_intereses), 0)     AS intereses_liquidados,
          COALESCE(SUM(gmf_lat.total_gmf), 0)           AS gmf_acumulado
        FROM prestamos.desembolsos d
          JOIN prestamos.operaciones o ON o.id = d.operacion_id
          LEFT JOIN LATERAL (
            SELECT t.saldo_capital
            FROM prestamos.tramos t
            WHERE t.operacion_id = o.id AND t.deleted_at IS NULL
            ORDER BY t.numero_tramo DESC
            LIMIT 1
          ) t_lat ON true
          LEFT JOIN LATERAL (
            SELECT COALESCE(SUM(lmd.intereses_periodo), 0) AS total_intereses
            FROM prestamos.liquidaciones_mensuales_detalle lmd
            WHERE lmd.operacion_id = o.id
          ) lmd_lat ON true
          LEFT JOIN LATERAL (
            SELECT COALESCE(SUM(gmf.monto_gmf), 0) AS total_gmf
            FROM prestamos.gmf_movimientos gmf
            WHERE gmf.operacion_id = o.id
          ) gmf_lat ON true
        WHERE o.estado_pipeline = 'DS'
          AND d.fecha >= ? AND d.fecha <= ?
          %s
        GROUP BY 1, 2
        ORDER BY 1, 2
        """.formatted(filtroEmpresa);

    return jdbc.query(sql,
        (rs, rowNum) -> {
          short anio = rs.getShort("anio");
          short mes  = rs.getShort("mes");
          return EvolucionMensualDto.builder()
              .anio(anio).mes(mes)
              .periodo(MESES_ES[mes] + " " + anio)
              .saldoCapital(rs.getBigDecimal("saldo_capital"))
              .interesesLiquidados(rs.getBigDecimal("intereses_liquidados"))
              .gmfAcumulado(rs.getBigDecimal("gmf_acumulado"))
              .build();
        },
        desde, hasta);
  }

  // ── KPIs ─────────────────────────────────────────────────────────

  @Override
  public KpiGerencialDto kpisGerenciales() {
    Long totalDs = (long) operacionRepo
        .findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS").size();

    Long rechazadas = (long) operacionRepo
        .findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("RECHAZADA").size();

    Long enPipeline = jdbc.queryForObject(
        "SELECT COUNT(*) FROM prestamos.operaciones " +
        "WHERE estado_pipeline IN ('CR','AI','AE','FD') AND deleted_at IS NULL", Long.class);

    Double diasProm = jdbc.queryForObject("""
        SELECT AVG(EXTRACT(EPOCH FROM (o.desembolso_at - o.created_at)) / 86400)
        FROM prestamos.operaciones o
        WHERE o.estado_pipeline = 'DS' AND o.desembolso_at IS NOT NULL
        """, Double.class);

    BigDecimal tasaProm = jdbc.queryForObject("""
        SELECT COALESCE(AVG(t.tasa_porcentaje_mensual), 0)
        FROM prestamos.tramos t
          JOIN prestamos.operaciones o ON o.id = t.operacion_id
        WHERE o.estado_pipeline = 'DS' AND t.estado = 'EN_CURSO'
          AND t.deleted_at IS NULL AND t.tipo_tasa != 'SIN_INTERES'
        """, BigDecimal.class);

    return KpiGerencialDto.builder()
        .diasPromedioAprobacion(diasProm != null ? Math.round(diasProm * 10.0) / 10.0 : 0.0)
        .operacionesRechazadas(rechazadas)
        .operacionesActivas(totalDs)
        .operacionesEnTramite(enPipeline != null ? enPipeline : 0)
        .tasaPromedioPonderada(tasaProm != null ? tasaProm : BigDecimal.ZERO)
        .build();
  }

  // ── helpers privados ─────────────────────────────────────────────

  private Map<String, Long> pipeline() {
    Map<String, Long> result = new LinkedHashMap<>();
    result.put("CR", 0L); result.put("AI", 0L);
    result.put("AE", 0L); result.put("FD", 0L); result.put("DS", 0L);

    jdbc.query("SELECT estado_pipeline, total FROM prestamos.vw_pipeline_dashboard",
        rs -> { result.put(rs.getString("estado_pipeline"), rs.getLong("total")); });
    return result;
  }

  private ConsolidadoDto consolidado() {
    BigDecimal derechosCapital = jdbc.queryForObject("""
        SELECT COALESCE(SUM(t.saldo_capital), 0)
        FROM prestamos.tramos t
          JOIN prestamos.operaciones o ON o.id = t.operacion_id
        WHERE o.estado_pipeline = 'DS' AND t.estado = 'EN_CURSO' AND t.deleted_at IS NULL
          AND o.empresa_prestamista_id = (SELECT id FROM prestamos.empresas WHERE codigo_interno = 'pluto' LIMIT 1)
        """, BigDecimal.class);

    BigDecimal obligCapital = jdbc.queryForObject("""
        SELECT COALESCE(SUM(t.saldo_capital), 0)
        FROM prestamos.tramos t
          JOIN prestamos.operaciones o ON o.id = t.operacion_id
        WHERE o.estado_pipeline = 'DS' AND t.estado = 'EN_CURSO' AND t.deleted_at IS NULL
          AND o.empresa_prestataria_id = (SELECT id FROM prestamos.empresas WHERE codigo_interno = 'pluto' LIMIT 1)
        """, BigDecimal.class);

    Long totalDs = (long) operacionRepo
        .findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS").size();

    BigDecimal dc = derechosCapital  != null ? derechosCapital  : BigDecimal.ZERO;
    BigDecimal oc = obligCapital     != null ? obligCapital     : BigDecimal.ZERO;

    return ConsolidadoDto.builder()
        .derechosSaldoCapital(dc)
        .derechosIntereses(BigDecimal.ZERO)
        .derechosTotal(dc)
        .obligacionesSaldoCapital(oc)
        .obligacionesIntereses(BigDecimal.ZERO)
        .obligacionesTotal(oc)
        .exposicionNeta(dc.subtract(oc))
        .totalOperacionesDs(totalDs)
        .build();
  }

  private List<AlertaDto> alertas() {
    return jdbc.query("""
        SELECT tipo, empresa_id, empresa_razon_social, subtipo,
               vigencia_hasta, dias_restantes, estado
        FROM prestamos.vw_alertas_tasas_por_vencer
        ORDER BY dias_restantes ASC
        """,
        (rs, rowNum) -> AlertaDto.builder()
            .tipo(rs.getString("tipo"))
            .subtipo(rs.getString("subtipo"))
            .empresaId(rs.getObject("empresa_id", Long.class))
            .empresaRazonSocial(rs.getString("empresa_razon_social"))
            .fechaVigenciaHasta(rs.getObject("vigencia_hasta", LocalDate.class))
            .diasRestantes(rs.getInt("dias_restantes"))
            .estado(rs.getString("estado"))
            .build());
  }

  private List<TasaVigenteDto> tasasVigentes() {
    return tasaPeriodoService.listarVigentesHoy().stream()
        .map(t -> TasaVigenteDto.builder()
            .tipoTasa(t.getTipoTasa())
            .porcentajeEfectivoAnual(t.getValorPorcentajeEfectivoAnual())
            .porcentajeMensual(t.getValorPorcentajeMensual())
            .vigenciaDesde(t.getVigenciaDesde())
            .vigenciaHasta(t.getVigenciaHasta())
            .build())
        .toList();
  }
}
