package co.pluto.dto.request.desembolso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ConfirmarDesembolsoRequestDto {

  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
  private BigDecimal monto;

  private LocalDate fecha;
}
