package co.pluto.dto.request.geo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DepartamentoRequestDto {
  @NotBlank @Size(min = 2, max = 2)
  private String codigoDane;

  @NotBlank @Size(max = 100)
  private String nombre;

  @NotNull
  private Integer paisId;
}
