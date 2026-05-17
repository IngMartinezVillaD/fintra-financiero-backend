package co.pluto.dto.request.tasas;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegistrarTasaRequestDto {

  @NotNull @Min(2024) @Max(2100)
  private Short anio;

  @NotNull @Min(1) @Max(12)
  private Short mes;

  @NotBlank
  @Pattern(regexp = "USURA|COMERCIAL_VIGENTE|PRESUNTA_FISCAL",
           message = "Debe ser USURA, COMERCIAL_VIGENTE o PRESUNTA_FISCAL")
  private String tipoTasa;

  @NotNull
  @DecimalMin(value = "0", inclusive = false, message = "La tasa EA debe ser mayor a 0")
  private BigDecimal valorPorcentajeEfectivoAnual;

  @NotNull
  @DecimalMin(value = "0", inclusive = false, message = "La tasa mensual debe ser mayor a 0")
  private BigDecimal valorPorcentajeMensual;

  @NotNull
  private LocalDate vigenciaDesde;

  @NotNull
  private LocalDate vigenciaHasta;

  private String observacion;
}
