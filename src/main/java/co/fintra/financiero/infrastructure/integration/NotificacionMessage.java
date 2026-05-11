package co.fintra.financiero.infrastructure.integration;

import java.util.Map;

public record NotificacionMessage(
    String evento,
    String titulo,
    String cuerpo,
    Map<String, Object> contextData
) {
  public static NotificacionMessage of(String evento, String titulo, String cuerpo,
                                        Map<String, Object> data) {
    return new NotificacionMessage(evento, titulo, cuerpo, data);
  }
}
