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
@Table(schema = "prestamos", name = "tasas_especiales_empresa")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TasaEspecialEmpresaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_id", nullable = false)
  private EmpresaEntity empresa;

  @Column(name = "valor_porcentaje_efectivo_anual", nullable = false, precision = 8, scale = 4)
  private BigDecimal valorPorcentajeEfectivoAnual;

  @Column(name = "valor_porcentaje_mensual", nullable = false, precision = 8, scale = 4)
  private BigDecimal valorPorcentajeMensual;

  @Column(name = "vigencia_desde", nullable = false)
  private LocalDate vigenciaDesde;

  @Column(name = "vigencia_hasta", nullable = false)
  private LocalDate vigenciaHasta;

  @Column(nullable = false, length = 20)
  private String estado = "PENDIENTE";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aprobado_por_usuario_id")
  private UsuarioEntity aprobadoPor;

  @Column(name = "aprobado_at")
  private OffsetDateTime aprobadoAt;

  @Column(columnDefinition = "TEXT")
  private String observacion;

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
