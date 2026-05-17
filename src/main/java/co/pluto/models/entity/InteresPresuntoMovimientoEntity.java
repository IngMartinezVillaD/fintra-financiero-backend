package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "interes_presunto_movimientos")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InteresPresuntoMovimientoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "empresa_id", nullable = false)
  private Long empresaId;

  @Column(name = "operacion_id", nullable = false)
  private Long operacionId;

  @Column(nullable = false)
  private Short anio;

  @Column(nullable = false)
  private Short mes;

  @Column(name = "saldo_capital_promedio", nullable = false, precision = 19, scale = 6)
  private BigDecimal saldoCapitalPromedio;

  @Column(name = "tasa_presunta_porcentaje", nullable = false, precision = 8, scale = 4)
  private BigDecimal tasaPresuntaPorcentaje;

  @Column(nullable = false)
  private Integer dias;

  @Column(name = "monto_calculado", nullable = false, precision = 19, scale = 6)
  private BigDecimal montoCalculado;

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
