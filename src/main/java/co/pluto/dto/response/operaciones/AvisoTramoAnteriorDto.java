package co.pluto.dto.response.operaciones;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class AvisoTramoAnteriorDto {
  private Long operacionId;
  private String referencia;
  private Long tramoId;
  private BigDecimal saldoCapital;
  private LocalDate fechaDesdeTramo;
  private int diasTranscurridos;
  private BigDecimal tasaMensual;
  private String tipoTasa;
  private BigDecimal interesEstimado;
}
