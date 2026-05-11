package co.fintra.financiero.dto.response.seguimiento;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class TramoDto {
  private Long id;
  private Integer numeroTramo;
  private String tipoMovimiento;
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;
  private Integer dias;
  private BigDecimal saldoCapital;
  private BigDecimal tasaPorcentajeMensual;
  private String tipoTasa;
  private BigDecimal interesCalculado;
  private String estado;
}
