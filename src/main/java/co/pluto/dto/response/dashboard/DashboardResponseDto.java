package co.pluto.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder
public class DashboardResponseDto {
  private Map<String, Long>     pipeline;
  private ConsolidadoDto        consolidado;
  private List<AlertaDto>       alertas;
  private List<TasaVigenteDto>  tasasVigentes;
}
