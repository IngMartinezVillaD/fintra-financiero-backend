package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.response.dashboard.ConsolidadoEmpresaDto;
import co.fintra.financiero.services.interfaces.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

  private final JdbcTemplate jdbc;

  @Override
  @Cacheable("empresasActivas")
  public List<ConsolidadoEmpresaDto> consolidadoFinanciero() {
    return jdbc.query("""
        SELECT
          empresa_id,
          codigo_interno,
          razon_social,
          nit,
          total_operaciones,
          saldo_capital_vigente,
          intereses_causados_pendientes,
          total_desembolsado,
          ultimo_desembolso_fecha
        FROM prestamos.vw_consolidado_financiero
        ORDER BY razon_social
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
            .build()
    );
  }
}
