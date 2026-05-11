package co.fintra.financiero.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class TasaVigenteDto {
  private String tipoTasa;
  private BigDecimal porcentajeEfectivoAnual;
  private BigDecimal porcentajeMensual;
  private LocalDate vigenciaDesde;
  private LocalDate vigenciaHasta;
}
