package co.fintra.financiero.dto.response.operaciones;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data @Builder
public class EventoPipelineDto {
  private String estadoAnterior;
  private String estadoNuevo;
  private String usuario;
  private String observacion;
  private OffsetDateTime ocurridoAt;
}
