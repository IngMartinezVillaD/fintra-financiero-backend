package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "eventos_pipeline")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventoPipelineEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "operacion_id", nullable = false)
  private OperacionEntity operacion;

  @Column(name = "estado_anterior", length = 20)
  private String estadoAnterior;

  @Column(name = "estado_nuevo", nullable = false, length = 20)
  private String estadoNuevo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private UsuarioEntity usuario;

  @Column(columnDefinition = "TEXT")
  private String observacion;

  @Column(name = "ocurrido_at", nullable = false)
  private OffsetDateTime ocurridoAt;
}
