package co.fintra.financiero.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class KpiGerencialDto {
  private Double diasPromedioAprobacion;
  private Long operacionesRechazadas;
  private Long operacionesActivas;
  private Long operacionesEnPipeline;
  private BigDecimal tasaPromedioPonderada;
}
