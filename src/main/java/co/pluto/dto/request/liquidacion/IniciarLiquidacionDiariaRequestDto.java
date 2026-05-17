package co.pluto.dto.request.liquidacion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IniciarLiquidacionDiariaRequestDto {

  @NotNull(message = "La fecha es obligatoria")
  private LocalDate fecha;
}
