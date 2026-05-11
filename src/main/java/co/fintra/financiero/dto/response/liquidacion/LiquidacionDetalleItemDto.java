package co.fintra.financiero.dto.response.liquidacion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class LiquidacionDetalleItemDto {
  private Long id;
  private Long operacionId;
  private String referencia;
  private String empresaPrestatariaNombre;
  private String empresaPrestamistaNombre;
  private BigDecimal interesesPeriodo;
  private BigDecimal retencionFuenteAplicada;
  private BigDecimal retencionIcaAplicada;
  private BigDecimal netoCobrar;
}
