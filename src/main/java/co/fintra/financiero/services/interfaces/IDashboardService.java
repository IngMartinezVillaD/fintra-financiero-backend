package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.response.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface IDashboardService {

  List<ConsolidadoEmpresaDto> consolidadoFinanciero();

  DashboardResponseDto dashboard();

  List<EvolucionMensualDto> evolucionMensual(LocalDate desde, LocalDate hasta, Long empresaId);

  KpiGerencialDto kpisGerenciales();
}
