package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "seguridad", name = "auditoria_login")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLoginEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String username;

  @Column(nullable = false)
  private boolean exitoso;

  @Column(length = 45)
  private String ip;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}
