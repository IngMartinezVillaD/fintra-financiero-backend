package co.pluto.dto.response.desembolso;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class TramoInicialDto {
  private Long id;
  private Integer numeroTramo;
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;
  private Integer dias;
  private BigDecimal saldoCapital;
  private BigDecimal tasaPorcentajeMensual;
  private String tipoTasa;
  private BigDecimal interesCalculado;
}
