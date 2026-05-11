package co.fintra.financiero.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class EvolucionMensualDto {
  private Short anio;
  private Short mes;
  private String periodo;
  private BigDecimal saldoCapital;
  private BigDecimal interesesLiquidados;
  private BigDecimal gmfAcumulado;
}
