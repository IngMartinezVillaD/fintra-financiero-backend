package co.pluto.dto.request.operaciones;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CrearOperacionRequestDto {

  @NotNull
  private Long empresaPrestamistaId;

  @NotNull
  private Long empresaPrestatariaId;

  @NotNull(message = "El cupo rotativo es obligatorio")
  private Long cupoRotativoId;

  @NotBlank
  @Pattern(regexp = "SI_COMERCIAL|SI_ESPECIAL|NO",
           message = "Debe ser SI_COMERCIAL, SI_ESPECIAL o NO")
  private String cobraInteres;

  private Long cuentaOrigenId;
  private Long cuentaDestinoId;

  @DecimalMin(value = "0", inclusive = false, message = "El monto estimado debe ser mayor a 0")
  private BigDecimal montoEstimado;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate fechaVencimiento;

  @Pattern(regexp = "BULLET|CUOTAS")
  private String formaPago;

  @Min(1)
  private Short numCuotas;

  @NotBlank @Size(max = 2000)
  private String observaciones;

  @NotBlank @Size(max = 60)
  private String numDocumentoSoporte;
}
