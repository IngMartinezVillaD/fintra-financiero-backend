package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "integraciones", name = "bitrix24_notificaciones")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bitrix24NotificacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "evento_codigo", nullable = false, length = 60)
  private String eventoCodigo;

  @Column(nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Column(nullable = false, length = 20)
  private String estado = "PENDIENTE";

  @Column(nullable = false)
  private Short reintentos = 0;

  @Column(name = "ultimo_error", columnDefinition = "TEXT")
  private String ultimoError;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
