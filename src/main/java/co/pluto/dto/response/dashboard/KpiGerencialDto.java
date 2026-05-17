package co.pluto.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class KpiGerencialDto {
  private Double diasPromedioAprobacion;
  private Long operacionesRechazadas;
  private Long operacionesActivas;
  private Long operacionesEnTramite;
  private BigDecimal tasaPromedioPonderada;
}
