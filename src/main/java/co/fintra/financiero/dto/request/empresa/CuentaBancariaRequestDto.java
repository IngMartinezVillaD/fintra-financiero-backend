package co.fintra.financiero.dto.request.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CuentaBancariaRequestDto {

  @NotBlank
  private String bancoCodigo;

  @NotBlank
  @Pattern(regexp = "CORRIENTE|AHORROS", message = "Debe ser CORRIENTE o AHORROS")
  private String tipo;

  @NotBlank @Size(max = 30)
  private String numeroCuenta;

  @NotBlank @Size(max = 200)
  private String titular;

  @Size(max = 30)
  private String codigoContable;

  @Size(max = 50)
  private String formatoArchivoPlano;

  private Boolean exentaGmf = false;
}
