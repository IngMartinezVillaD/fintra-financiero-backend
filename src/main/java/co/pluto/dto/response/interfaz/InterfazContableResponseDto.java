package co.pluto.dto.response.interfaz;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class InterfazContableResponseDto {
  private Long   id;
  private Long   empresaId;
  private String empresaNombre;
  private String empresaNit;
  private Long   tipoMovimientoId;
  private String tipoMovimientoCodigo;
  private String tipoMovimientoNombre;
  private String nombre;
  private String descripcion;
  private boolean activa;
  private OffsetDateTime createdAt;
  private List<LineaDto> lineas;

  @Data
  @Builder
  public static class LineaDto {
    private Long   id;
    private Short  orden;
    private Long   cuentaPucId;
    private String cuentaPucCodigo;
    private String cuentaPucNombre;
    private String naturaleza;
    private String descripcionGlosa;
  }
}
