package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "prestamos", name = "asientos_contables")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsientoContableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tipo_origen", nullable = false, length = 20)
  private String tipoOrigen;   // LIQUIDACION | DESEMBOLSO

  @Column(name = "origen_id", nullable = false)
  private Long origenId;

  @Column(name = "empresa_id", nullable = false)
  private Long empresaId;

  @Column(name = "interfaz_id", nullable = false)
  private Long interfazId;

  @Column(nullable = false)
  private LocalDate fecha;

  @Column(length = 300)
  private String descripcion;

  @Column(nullable = false, length = 20)
  private String estado;  // GENERADO | ANULADO

  @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @OrderBy("orden ASC")
  @Builder.Default
  private List<AsientoContableDetalleEntity> lineas = new ArrayList<>();

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;
}
