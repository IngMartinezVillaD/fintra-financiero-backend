package co.pluto.dto.request.cupos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActualizarCupoRotativoRequestDto {

  @NotNull(message = "El valor del cupo es obligatorio")
  @DecimalMin(value = "1", message = "El valor del cupo debe ser mayor a 0")
  private BigDecimal valorCupo;

  @NotBlank(message = "El estado es obligatorio")
  @Pattern(regexp = "ACTIVO|SUSPENDIDO|CERRADO",
           message = "Debe ser ACTIVO, SUSPENDIDO o CERRADO")
  private String estado;

  @Size(max = 1000)
  private String observaciones;
}
