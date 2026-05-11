package co.fintra.financiero.dto.request.liquidacion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IniciarLiquidacionRequestDto {

  @NotNull(message = "El año es obligatorio")
  @Min(value = 2020, message = "Año inválido")
  private Short anio;

  @NotNull(message = "El mes es obligatorio")
  @Min(value = 1) @Max(value = 12)
  private Short mes;
}
