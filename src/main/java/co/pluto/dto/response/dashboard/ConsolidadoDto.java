package co.pluto.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class ConsolidadoDto {
  private BigDecimal derechosSaldoCapital;
  private BigDecimal derechosIntereses;
  private BigDecimal derechosTotal;

  private BigDecimal obligacionesSaldoCapital;
  private BigDecimal obligacionesIntereses;
  private BigDecimal obligacionesTotal;

  private BigDecimal exposicionNeta;
  private Long totalOperacionesDs;
}
