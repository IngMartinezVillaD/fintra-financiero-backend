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
@Table(schema = "prestamos", name = "abonos")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbonoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "operacion_id", nullable = false)
  private Long operacionId;

  @Column(nullable = false)
  private LocalDate fecha;

  @Column(name = "monto_total", nullable = false, precision = 19, scale = 6)
  private BigDecimal montoTotal;

  @Column(name = "numero_comprobante", nullable = false, length = 60)
  private String numeroComprobante;

  @Column(name = "aplicado_a_intereses", nullable = false, precision = 19, scale = 6)
  private BigDecimal aplicadoAIntereses = BigDecimal.ZERO;

  @Column(name = "aplicado_a_capital", nullable = false, precision = 19, scale = 6)
  private BigDecimal aplicadoACapital = BigDecimal.ZERO;

  @Column(name = "tramo_liquidado_id")
  private Long tramoLiquidadoId;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  @Column(columnDefinition = "TEXT")
  private String observaciones;

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
