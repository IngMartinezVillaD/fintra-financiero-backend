package co.pluto.dto.response.interfaz;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TipoMovimientoContableDto {
  private Long   id;
  private String codigo;
  private String nombre;
  private String descripcion;
}
