package co.fintra.financiero.models.entity;

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
@Table(schema = "prestamos", name = "tramos")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TramoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "operacion_id", nullable = false)
  private OperacionEntity operacion;

  @Column(name = "numero_tramo", nullable = false)
  private Integer numeroTramo;

  @Column(name = "tipo_movimiento", nullable = false, length = 50)
  private String tipoMovimiento;

  @Column(name = "fecha_desde", nullable = false)
  private LocalDate fechaDesde;

  @Column(name = "fecha_hasta", nullable = false)
  private LocalDate fechaHasta;

  @Column(name = "saldo_capital", nullable = false, precision = 19, scale = 6)
  private BigDecimal saldoCapital;

  @Column(nullable = false)
  private Integer dias;

  @Column(name = "tasa_porcentaje_mensual", nullable = false, precision = 8, scale = 4)
  private BigDecimal tasaPorcentajeMensual;

  @Column(name = "tipo_tasa", nullable = false, length = 20)
  private String tipoTasa;

  @Column(name = "interes_calculado", nullable = false, precision = 19, scale = 6)
  private BigDecimal interesCalculado = BigDecimal.ZERO;

  @Column(nullable = false, length = 20)
  private String estado = "EN_CURSO";

  @Column(name = "liquidacion_id")
  private Long liquidacionId;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

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
