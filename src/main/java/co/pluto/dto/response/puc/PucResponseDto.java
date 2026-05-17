package co.pluto.dto.response.puc;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PucResponseDto {
  private Long   id;
  private String codigo;
  private String nombre;
  private String tipo;
  private String naturaleza;
  private Short  nivel;
  private String nivelNombre;
  private boolean activa;
  private boolean aplicaCentroCosto;
  private OffsetDateTime createdAt;
}
