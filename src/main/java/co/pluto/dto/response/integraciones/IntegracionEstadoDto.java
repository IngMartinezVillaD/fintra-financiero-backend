package co.pluto.dto.response.integraciones;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data @Builder
public class IntegracionEstadoDto {
  private String nombre;
  private String estado;       // OK | DEGRADADO | CAIDO | DESACTIVADO
  private boolean activo;
  private long enviosExitosos24h;
  private long errores24h;
  private String ultimoMensaje;
  private OffsetDateTime ultimaActividad;
}
