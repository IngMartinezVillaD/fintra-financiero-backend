package co.fintra.financiero.infrastructure;

public interface NotificacionPort {
  void enviar(NotificacionMessage message);
}
