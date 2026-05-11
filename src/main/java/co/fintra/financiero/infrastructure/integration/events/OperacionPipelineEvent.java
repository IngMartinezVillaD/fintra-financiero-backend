package co.fintra.financiero.infrastructure.integration.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OperacionPipelineEvent extends ApplicationEvent {

  private final Long operacionId;
  private final String referencia;
  private final String estadoNuevo;
  private final String eventoTipo;

  public OperacionPipelineEvent(Object source, Long operacionId, String referencia,
                                 String estadoNuevo, String eventoTipo) {
    super(source);
    this.operacionId  = operacionId;
    this.referencia   = referencia;
    this.estadoNuevo  = estadoNuevo;
    this.eventoTipo   = eventoTipo;
  }

  public static final String ENVIADA_APROBACION     = "OPERACION_ENVIADA_APROBACION";
  public static final String APROBADA_INTERNAMENTE  = "OPERACION_APROBADA_INTERNAMENTE";
  public static final String DEVUELTA               = "OPERACION_DEVUELTA";
  public static final String RECHAZADA              = "OPERACION_RECHAZADA";
  public static final String ACEPTADA_EMPRESA       = "OPERACION_ACEPTADA_EMPRESA";
  public static final String FIRMA_COMPLETADA       = "FIRMA_COMPLETADA";
  public static final String DESEMBOLSO_CONFIRMADO  = "DESEMBOLSO_CONFIRMADO";
}
