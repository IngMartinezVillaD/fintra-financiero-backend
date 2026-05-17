package co.pluto.dto.request.puc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CrearPucRequestDto {

  @NotBlank(message = "El código es obligatorio")
  @Pattern(
    regexp = "^\\d{1}$|^\\d{2}$|^\\d{4}$|^\\d{6}$|^\\d{8}$",
    message = "El código debe tener 1, 2, 4, 6 u 8 dígitos"
  )
  private String codigo;

  @NotBlank(message = "El nombre es obligatorio")
  private String nombre;

  @NotBlank(message = "El tipo es obligatorio")
  @Pattern(
    regexp = "ACTIVO|PASIVO|PATRIMONIO|INGRESO|GASTO|COSTO_VENTA|COSTO_PRODUCCION|ORDEN_DEUDORA|ORDEN_ACREEDORA",
    message = "Tipo inválido"
  )
  private String tipo;

  @NotBlank(message = "La naturaleza es obligatoria")
  @Pattern(regexp = "DEBITO|CREDITO", message = "Naturaleza debe ser DEBITO o CREDITO")
  private String naturaleza;

  private Boolean aplicaCentroCosto = false;
}
