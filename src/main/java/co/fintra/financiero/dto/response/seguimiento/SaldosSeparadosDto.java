package co.fintra.financiero.dto.response.seguimiento;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class SaldosSeparadosDto {
  private BigDecimal saldoCapital;
  private BigDecimal interesesCausados;
  private BigDecimal interesEnCurso;
  private BigDecimal gmfIncurrido;
  private BigDecimal deudaTotal;
}
