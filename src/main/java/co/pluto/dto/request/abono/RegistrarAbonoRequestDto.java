package co.pluto.dto.request.abono;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegistrarAbonoRequestDto {

  @NotNull(message = "La fecha del abono es obligatoria")
  private LocalDate fechaAbono;

  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
  private BigDecimal monto;

  @NotBlank(message = "El número de comprobante es obligatorio")
  private String numeroComprobante;

  private String observaciones;
}
