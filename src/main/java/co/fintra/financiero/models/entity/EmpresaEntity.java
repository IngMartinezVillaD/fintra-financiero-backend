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
@Table(schema = "prestamos", name = "empresas")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpresaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "codigo_interno", nullable = false, unique = true, length = 20)
  private String codigoInterno;

  @Column(name = "razon_social", nullable = false, length = 200)
  private String razonSocial;

  @Column(nullable = false, unique = true, length = 20)
  private String nit;

  @Column(nullable = false, length = 100)
  private String pais = "Colombia";

  @Column(length = 100)
  private String ciudad;

  @Column(length = 100)
  private String departamento;

  @Column(name = "rol_permitido", nullable = false, length = 20)
  private String rolPermitido;

  @Column(nullable = false, length = 20)
  private String estado = "ACTIVA";

  @Column(name = "representante_legal_nombre", length = 200)
  private String representanteLegalNombre;

  @Column(name = "representante_legal_email", length = 255)
  private String representanteLegalEmail;

  @Column(name = "representante_legal_telefono", length = 30)
  private String representanteLegalTelefono;

  @Column(name = "erp_utilizado", length = 20)
  private String erpUtilizado;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cuenta_cxc_id")
  private CuentaContableEntity cuentaCxc;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cuenta_cxp_id")
  private CuentaContableEntity cuentaCxp;

  @Column(name = "centro_utilidad", length = 50)
  private String centroUtilidad;

  @Column(name = "saldo_inicial_capital", nullable = false, precision = 19, scale = 6)
  private BigDecimal saldoInicialCapital = BigDecimal.ZERO;

  @Column(name = "saldo_inicial_intereses", nullable = false, precision = 19, scale = 6)
  private BigDecimal saldoInicialIntereses = BigDecimal.ZERO;

  @Column(name = "fecha_corte_saldo_inicial")
  private LocalDate fechaCorteSaldoInicial;

  @Column(name = "cobra_interes", nullable = false)
  private Boolean cobraInteres = false;

  @Column(name = "calcula_interes_presunto", nullable = false)
  private Boolean calculaInteresPresunto = false;

  @Column(name = "aplica_tasa_especial", nullable = false)
  private Boolean aplicaTasaEspecial = false;

  @Column(name = "retencion_fuente_porcentaje", precision = 5, scale = 2)
  private BigDecimal retencionFuentePorcentaje;

  @Column(name = "retencion_ica_porcentaje", precision = 5, scale = 2)
  private BigDecimal retencionIcaPorcentaje;

  @Column(length = 500)
  private String observaciones;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<EmpresaCuentaBancariaEntity> cuentasBancarias;

  @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<TasaEspecialEmpresaEntity> tasasEspeciales;

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
