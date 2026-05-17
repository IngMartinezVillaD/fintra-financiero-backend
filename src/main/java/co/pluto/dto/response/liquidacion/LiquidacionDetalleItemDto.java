package co.pluto.dto.response.liquidacion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class LiquidacionDetalleItemDto {
  private Long id;
  private Long operacionId;
  private Long saldoInicialId;
  private String referencia;
  private String tipoFuente; // "OPERACION" | "SALDO_INICIAL"
  private String empresaPrestatariaNombre;
  private String empresaPrestamistaNombre;
  private BigDecimal interesesPeriodo;
  private BigDecimal retencionFuenteAplicada;
  private BigDecimal retencionIcaAplicada;
  private BigDecimal netoCobrar;
}
