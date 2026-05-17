package co.pluto.dto.response.liquidacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiquidacionDiariaDetalleItemDto {
  private Long id;
  private Long operacionId;
  private Long saldoInicialId;
  private String tipoFuente; // "OPERACION" | "SALDO_INICIAL"
  private String referencia;
  private String empresaPrestatariaNombre;
  private String empresaPrestamistaNombre;
  private BigDecimal interesesPeriodo;
  private BigDecimal retencionFuenteAplicada;
  private BigDecimal retencionIcaAplicada;
  private BigDecimal netoCobrar;
}
