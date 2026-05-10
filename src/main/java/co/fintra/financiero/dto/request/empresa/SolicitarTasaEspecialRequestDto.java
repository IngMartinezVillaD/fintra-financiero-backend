package co.fintra.financiero.dto.request.empresa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SolicitarTasaEspecialRequestDto {

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
