package co.pluto.dto.request.desembolso;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenerarArchivoPlanoRequestDto {

  @NotNull(message = "La fecha de desembolso es obligatoria")
  private LocalDate fechaDesembolso;

  @NotEmpty(message = "Debe indicar al menos una operación")
  private List<Long> operacionIds;
}
