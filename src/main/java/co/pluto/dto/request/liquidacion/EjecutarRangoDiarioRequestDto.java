package co.pluto.dto.request.liquidacion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EjecutarRangoDiarioRequestDto {

  @NotNull(message = "fechaDesde es obligatoria")
  private LocalDate fechaDesde;

  @NotNull(message = "fechaHasta es obligatoria")
  private LocalDate fechaHasta;
}
