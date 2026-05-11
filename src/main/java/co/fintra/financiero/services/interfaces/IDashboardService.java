package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.response.dashboard.ConsolidadoEmpresaDto;

import java.util.List;

public interface IDashboardService {

  List<ConsolidadoEmpresaDto> consolidadoFinanciero();
}
