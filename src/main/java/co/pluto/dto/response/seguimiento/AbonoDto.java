package co.pluto.dto.response.seguimiento;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data @Builder
public class AbonoDto {
  private Long id;
  private LocalDate fecha;
  private BigDecimal montoTotal;
  private BigDecimal aplicadoAIntereses;
  private BigDecimal aplicadoACapital;
  private String numeroComprobante;
  private String observaciones;
  private Long tramoLiquidadoId;
  private OffsetDateTime createdAt;
}
