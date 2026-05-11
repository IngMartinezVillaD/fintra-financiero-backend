package co.fintra.financiero.dto.request.controles;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecisionAnualGmfRequestDto {

  @NotNull
  private Long empresaId;

  @NotNull
  private Short anio;

  @NotNull
  private String decision; // COBRAR | ASUMIR
}
