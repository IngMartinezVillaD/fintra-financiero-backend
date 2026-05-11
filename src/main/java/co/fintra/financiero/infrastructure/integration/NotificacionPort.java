package co.fintra.financiero.infrastructure.integration;

public interface NotificacionPort {
  void enviar(NotificacionMessage message);
}
