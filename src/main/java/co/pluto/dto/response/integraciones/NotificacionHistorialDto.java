package co.pluto.dto.response.integraciones;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data @Builder
public class NotificacionHistorialDto {
  private Long id;
  private String eventoCodigo;
  private String estado;
  private Short reintentos;
  private String ultimoError;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
