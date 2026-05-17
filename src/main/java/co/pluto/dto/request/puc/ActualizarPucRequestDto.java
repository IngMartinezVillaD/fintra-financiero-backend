package co.pluto.dto.request.puc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ActualizarPucRequestDto {

  @NotBlank(message = "El nombre es obligatorio")
  private String nombre;

  @Pattern(
    regexp = "ACTIVO|PASIVO|PATRIMONIO|INGRESO|GASTO|COSTO_VENTA|COSTO_PRODUCCION|ORDEN_DEUDORA|ORDEN_ACREEDORA",
    message = "Tipo inválido"
  )
  private String tipo;

  @Pattern(regexp = "DEBITO|CREDITO", message = "Naturaleza debe ser DEBITO o CREDITO")
  private String naturaleza;

  private Boolean aplicaCentroCosto;
}
