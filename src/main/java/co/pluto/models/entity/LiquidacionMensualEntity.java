package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "liquidaciones_mensuales")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiquidacionMensualEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Short anio;

  @Column(nullable = false)
  private Short mes;

  @Column(name = "fecha_corte", nullable = false)
  private LocalDate fechaCorte;

  @Column(nullable = false, length = 30)
  private String estado = "BORRADOR";

  @Column(name = "aprobada_por")
  private Long aprobadaPor;

  @Column(name = "aprobada_at")
  private OffsetDateTime aprobadaAt;

  @Column(name = "total_intereses_liquidados", nullable = false, precision = 19, scale = 6)
  private BigDecimal totalInteresesLiquidados = BigDecimal.ZERO;

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
