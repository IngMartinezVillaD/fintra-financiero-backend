package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "saldos_iniciales", schema = "prestamos")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoInicialEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "codigo", length = 20, nullable = false, unique = true)
  private String codigo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_prestamista_id", nullable = false)
  private EmpresaEntity empresaPrestamista;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_prestataria_id", nullable = false)
  private EmpresaEntity empresaPrestataria;

  @Column(name = "tipo_tasa", length = 30, nullable = false)
  private String tipoTasa;

  @Column(name = "tasa_porcentaje_mensual", precision = 8, scale = 4, nullable = false)
  private BigDecimal tasaPorcentajeMensual;

  @Column(name = "saldo_capital", precision = 19, scale = 6, nullable = false)
  private BigDecimal saldoCapital;

  @Column(name = "intereses_acumulados", precision = 19, scale = 6, nullable = false)
  private BigDecimal interesesAcumulados;

  @Column(name = "fecha_corte", nullable = false)
  private LocalDate fechaCorte;

  @Column(name = "estado", length = 20, nullable = false)
  private String estado;

  @Column(name = "observaciones", columnDefinition = "TEXT")
  private String observaciones;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @CreatedBy
  @Column(name = "created_by", length = 100, nullable = false, updatable = false)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = 100, nullable = false)
  private String updatedBy;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "deleted_at")
  private Instant deletedAt;
}
