package co.pluto.dto.request.geo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaisRequestDto {
  @NotBlank @Size(min = 2, max = 2) @Pattern(regexp = "[A-Z]{2}", message = "Debe ser código ISO-2 en mayúsculas")
  private String codigoIso2;

  @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}", message = "Debe ser código ISO-3 en mayúsculas")
  private String codigoIso3;

  @NotBlank @Size(max = 100)
  private String nombre;
}
