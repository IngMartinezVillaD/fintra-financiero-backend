package co.fintra.financiero.dto.request.operaciones;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearOperacionRequestDto {

  @NotNull
  private Long empresaPrestamistaId;

  @NotNull
  private Long empresaPrestatariaId;

  @NotBlank
  @Pattern(regexp = "SI_COMERCIAL|SI_ESPECIAL|NO",
           message = "Debe ser SI_COMERCIAL, SI_ESPECIAL o NO")
  private String cobraInteres;

  private Long cuentaOrigenId;
  private Long cuentaDestinoId;

  @DecimalMin(value = "0", inclusive = false, message = "El monto estimado debe ser mayor a 0")
  private BigDecimal montoEstimado;

  @NotBlank @Size(max = 2000)
  private String observaciones;

  @NotBlank @Size(max = 60)
  private String numDocumentoSoporte;
}
