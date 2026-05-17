package co.pluto.dto.request.geo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CiudadRequestDto {
  @NotBlank @Size(min = 5, max = 5)
  private String codigoDane;

  @NotBlank @Size(max = 120)
  private String nombre;

  @Size(min = 6, max = 6, message = "El código postal debe tener exactamente 6 dígitos")
  @Pattern(regexp = "\\d{6}", message = "El código postal debe ser numérico de 6 dígitos")
  private String codigoPostal;

  @NotNull
  private Integer departamentoId;
}
