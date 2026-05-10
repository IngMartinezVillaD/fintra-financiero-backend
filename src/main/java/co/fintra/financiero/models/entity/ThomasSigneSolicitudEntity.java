package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "integraciones", name = "thomas_signe_solicitudes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThomasSigneSolicitudEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "operacion_id", nullable = false)
  private Long operacionId;

  @Column(name = "documento_url", nullable = false, columnDefinition = "TEXT")
  private String documentoUrl;

  @Column(name = "destinatario_email", nullable = false, length = 255)
  private String destinatarioEmail;

  @Column(nullable = false, length = 20)
  private String estado = "ENVIADA";

  @Column(name = "enviado_at")
  private OffsetDateTime enviadoAt;

  @Column(name = "firmado_at")
  private OffsetDateTime firmadoAt;

  @Column(name = "webhook_payload", columnDefinition = "JSONB")
  private String webhookPayload;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
  private String idempotencyKey;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;

  @Version
  @Column(nullable = false)
  private Long version;
}
