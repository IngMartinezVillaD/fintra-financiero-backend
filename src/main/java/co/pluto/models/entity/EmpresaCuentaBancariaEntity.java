package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "empresa_cuentas_bancarias")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpresaCuentaBancariaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "empresa_id", nullable = false)
  private EmpresaEntity empresa;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "banco_codigo", referencedColumnName = "codigo", nullable = false)
  private BancoEntity banco;

  @Column(nullable = false, length = 20)
  private String tipo;

  @Column(name = "numero_cuenta", nullable = false, length = 30)
  private String numeroCuenta;

  @Column(nullable = false, length = 200)
  private String titular;

  @Column(name = "codigo_contable", length = 30)
  private String codigoContable;

  @Column(name = "formato_archivo_plano", length = 50)
  private String formatoArchivoPlano;

  @Column(name = "exenta_gmf", nullable = false)
  private Boolean exentaGmf = false;

  @Column(nullable = false)
  private Boolean activa = true;

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
