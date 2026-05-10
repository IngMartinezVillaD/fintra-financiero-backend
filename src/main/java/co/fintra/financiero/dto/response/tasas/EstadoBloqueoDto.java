package co.fintra.financiero.dto.response.tasas;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class EstadoBloqueoDto {
  private String estado;       // OPERATIVO | BLOQUEADO_GLOBAL | BLOQUEADO_EMPRESA
  private String motivo;
  private String tasaTipo;
  private String vigenciaUltima;
  private String ruta;
  private String rutaLabel;
}
