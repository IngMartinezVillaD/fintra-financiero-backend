package co.pluto.dto.request.interfaz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class CrearInterfazContableRequestDto {

  @NotNull(message = "La empresa es obligatoria")
  private Long empresaId;

  @NotNull(message = "El tipo de movimiento es obligatorio")
  private Long tipoMovimientoId;

  @NotBlank(message = "El nombre es obligatorio")
  private String nombre;

  private String descripcion;

  @NotEmpty(message = "Debe incluir al menos una línea contable")
  @Valid
  private List<LineaDto> lineas;

  @Data
  public static class LineaDto {

    @NotNull(message = "La cuenta PUC es obligatoria")
    private Long cuentaPucId;

    @NotBlank(message = "La naturaleza es obligatoria")
    @Pattern(regexp = "DEBITO|CREDITO", message = "Naturaleza debe ser DEBITO o CREDITO")
    private String naturaleza;

    private String descripcionGlosa;

    @NotNull(message = "El orden es obligatorio")
    private Short orden;
  }
}
