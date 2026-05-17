package co.pluto.dto.response.contabilizacion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder
public class AsientoContableResponseDto {
  private Long       id;
  private String     tipoOrigen;
  private Long       origenId;
  private Long       empresaId;
  private String     empresaNombre;
  private Long       interfazId;
  private String     interfazNombre;
  private LocalDate  fecha;
  private String     descripcion;
  private String     estado;
  private OffsetDateTime createdAt;
  private String     createdBy;
  private List<AsientoContableDetalleDto> lineas;
}
