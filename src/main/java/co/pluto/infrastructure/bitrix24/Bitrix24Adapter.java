package co.pluto.infrastructure.bitrix24;

import co.pluto.infrastructure.NotificacionMessage;
import co.pluto.infrastructure.NotificacionPort;
import co.pluto.infrastructure.events.OperacionPipelineEvent;
import co.pluto.models.entity.Bitrix24NotificacionEntity;
import co.pluto.models.repositories.IBitrix24NotificacionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bitrix24Adapter implements NotificacionPort {

  @Value("${integraciones.bitrix24.activo:false}")
  private boolean activo;

  @Value("${integraciones.bitrix24.webhook-url:}")
  private String webhookUrl;

  private final IBitrix24NotificacionRepository repo;
  private final ObjectMapper                    mapper;

  // ── Listener de eventos de dominio ───────────────────────────────

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void onOperacionPipeline(OperacionPipelineEvent evento) {
    String titulo = tituloPorEvento(evento.getEventoTipo());
    enviar(NotificacionMessage.of(
        evento.getEventoTipo(), titulo,
        "Operación " + evento.getReferencia() + " → " + evento.getEstadoNuevo(),
        Map.of("operacionId", evento.getOperacionId(), "referencia", evento.getReferencia())
    ));
  }

  // ── NotificacionPort ─────────────────────────────────────────────

  @Override
  public void enviar(NotificacionMessage msg) {
    Bitrix24NotificacionEntity registro = persistir(msg, "PENDIENTE", null);

    if (!activo) {
      log.debug("Bitrix24 inactivo — notificación {} registrada como PENDIENTE", msg.evento());
      return;
    }

    enviarConRetry(registro, msg);
  }

  @Retryable(retryFor = Exception.class, maxAttempts = 3,
             backoff = @Backoff(delay = 1000, multiplier = 2))
  private void enviarConRetry(Bitrix24NotificacionEntity registro, NotificacionMessage msg) {
    try {
      RestClient.create(webhookUrl).post()
          .body(Map.of("event", msg.evento(), "title", msg.titulo(),
                        "body", msg.cuerpo(), "data", msg.contextData()))
          .retrieve().toBodilessEntity();

      registro.setEstado("ENVIADA");
      repo.save(registro);
      log.info("Bitrix24 notificación enviada: {}", msg.evento());
    } catch (Exception e) {
      registro.setReintentos((short) (registro.getReintentos() + 1));
      registro.setUltimoError(e.getMessage());
      repo.save(registro);
      throw e;
    }
  }

  @Recover
  private void recuperar(Exception e, Bitrix24NotificacionEntity registro, NotificacionMessage msg) {
    registro.setEstado("ERROR");
    registro.setUltimoError("Agotados los reintentos: " + e.getMessage());
    repo.save(registro);
    log.error("Bitrix24 falló definitivamente para evento {}: {}", msg.evento(), e.getMessage());
  }

  // ── helpers ──────────────────────────────────────────────────────

  private Bitrix24NotificacionEntity persistir(NotificacionMessage msg, String estado, String error) {
    String payloadJson;
    try {
      payloadJson = mapper.writeValueAsString(Map.of(
          "evento", msg.evento(), "titulo", msg.titulo(),
          "cuerpo", msg.cuerpo(), "data", msg.contextData()
      ));
    } catch (JsonProcessingException ex) {
      payloadJson = "{\"error\":\"serialization_failed\"}";
    }
    return repo.save(Bitrix24NotificacionEntity.builder()
        .eventoCodigo(msg.evento())
        .payload(payloadJson)
        .estado(estado)
        .reintentos((short) 0)
        .ultimoError(error)
        .build());
  }

  private String tituloPorEvento(String tipo) {
    return switch (tipo) {
      case OperacionPipelineEvent.ENVIADA_APROBACION    -> "Nueva operación pendiente de aprobación";
      case OperacionPipelineEvent.APROBADA_INTERNAMENTE -> "Operación aprobada — pendiente aceptación empresa";
      case OperacionPipelineEvent.DEVUELTA              -> "Operación devuelta — requiere ajuste";
      case OperacionPipelineEvent.RECHAZADA             -> "Operación rechazada";
      case OperacionPipelineEvent.ACEPTADA_EMPRESA      -> "Operación aceptada — pendiente firma";
      case OperacionPipelineEvent.FIRMA_COMPLETADA      -> "Firma completada — listo para desembolso";
      case OperacionPipelineEvent.DESEMBOLSO_CONFIRMADO -> "Desembolso confirmado";
      default -> tipo;
    };
  }
}
