package co.pluto.dto.request.saldos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CrearSaldoInicialRequestDto {

  @NotNull(message = "La empresa prestamista es obligatoria")
  private Long empresaPrestamistaId;

  @NotNull(message = "La empresa prestataria es obligatoria")
  private Long empresaPrestatariaId;

  @NotBlank(message = "El tipo de tasa es obligatorio")
  @Pattern(regexp = "COMERCIAL_VIGENTE|PRESUNTA_FISCAL|ESPECIAL",
           message = "Debe ser COMERCIAL_VIGENTE, PRESUNTA_FISCAL o ESPECIAL")
  private String tipoTasa;

  @NotNull(message = "La tasa mensual es obligatoria")
  @DecimalMin(value = "0.0001", message = "La tasa debe ser mayor a 0")
  private BigDecimal tasaPorcentajeMensual;

  @NotNull(message = "El saldo capital es obligatorio")
  @DecimalMin(value = "0", inclusive = true, message = "El saldo capital no puede ser negativo")
  private BigDecimal saldoCapital;

  @DecimalMin(value = "0", inclusive = true, message = "Los intereses acumulados no pueden ser negativos")
  private BigDecimal interesesAcumulados;

  @NotNull(message = "La fecha de corte es obligatoria")
  private LocalDate fechaCorte;

  @Size(max = 2000)
  private String observaciones;
}
