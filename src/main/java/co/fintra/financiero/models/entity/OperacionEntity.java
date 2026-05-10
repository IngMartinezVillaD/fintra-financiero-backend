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
import java.util.List;

@Entity
@Table(schema = "prestamos", name = "operaciones")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OperacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, length = 20)
  private String referencia; // generada por trigger DB, no setear manualmente

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_prestamista_id", nullable = false)
  private EmpresaEntity empresaPrestamista;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_prestataria_id", nullable = false)
  private EmpresaEntity empresaPrestataria;

  @Column(name = "cobra_interes", nullable = false, length = 20)
  private String cobraInteres = "NO";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cuenta_origen_id")
  private EmpresaCuentaBancariaEntity cuentaOrigen;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cuenta_destino_id")
  private EmpresaCuentaBancariaEntity cuentaDestino;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String observaciones;

  @Column(name = "num_documento_soporte", nullable = false, length = 60)
  private String numDocumentoSoporte;

  @Column(name = "monto_estimado", precision = 19, scale = 6)
  private BigDecimal montoEstimado;

  @Column(name = "estado_pipeline", nullable = false, length = 20)
  private String estadoPipeline = "CR";

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDate fechaCreacion;

  // Aprobación interna (AI)
  @Column(name = "aprobacion_interna_at")
  private OffsetDateTime aprobacionInternaAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aprobacion_interna_usuario_id")
  private UsuarioEntity aprobacionInternaUsuario;

  @Column(name = "aprobacion_interna_observacion", columnDefinition = "TEXT")
  private String aprobacionInternaObservacion;

  // Aceptación empresa (AE)
  @Column(name = "aceptacion_empresa_at")
  private OffsetDateTime aceptacionEmpresaAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aceptacion_empresa_usuario_id")
  private UsuarioEntity aceptacionEmpresaUsuario;

  @Column(name = "aceptacion_empresa_observacion", columnDefinition = "TEXT")
  private String aceptacionEmpresaObservacion;

  // Firma digital (FD)
  @Column(name = "firma_digital_at")
  private OffsetDateTime firmaDigitalAt;

  // Desembolso (DS)
  @Column(name = "desembolso_at")
  private OffsetDateTime desembolsoAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @OneToMany(mappedBy = "operacion")
  private List<TramoEntity> tramos;

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
