package co.pluto.dto.request.cupos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearCupoRotativoRequestDto {

  @NotNull(message = "La empresa es obligatoria")
  private Long empresaId;

  @NotBlank(message = "El tipo de tasa es obligatorio")
  @Pattern(regexp = "COMERCIAL_VIGENTE|PRESUNTA_FISCAL|ESPECIAL",
           message = "Debe ser COMERCIAL_VIGENTE, PRESUNTA_FISCAL o ESPECIAL")
  private String tipoTasa;

  @NotNull(message = "La tasa mensual es obligatoria")
  @DecimalMin(value = "0.0001", message = "La tasa debe ser mayor a 0")
  private BigDecimal tasaPorcentajeMensual;

  @NotNull(message = "El valor del cupo es obligatorio")
  @DecimalMin(value = "1", message = "El valor del cupo debe ser mayor a 0")
  private BigDecimal valorCupo;

  @Size(max = 1000)
  private String observaciones;
}
