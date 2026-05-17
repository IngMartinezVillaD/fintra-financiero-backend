package co.pluto.dto.response.desembolso;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class GmfResumenDto {
  private Boolean aplica;
  private BigDecimal monto;
  private BigDecimal tarifa;
  private String motivoExencion;
}
