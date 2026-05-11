package co.fintra.financiero.dto.response.controles;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class PresuntoMensualItemDto {
  private Long id;
  private Long operacionId;
  private String referencia;
  private Short mes;
  private BigDecimal saldoCapitalPromedio;
  private BigDecimal tasaPresuntaPorcentaje;
  private Integer dias;
  private BigDecimal montoCalculado;
}
