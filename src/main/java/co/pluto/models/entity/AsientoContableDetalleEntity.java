package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "prestamos", name = "asientos_contables_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsientoContableDetalleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asiento_id", nullable = false)
  private AsientoContableEntity asiento;

  @Column(nullable = false)
  private Short orden;

  @Column(name = "cuenta_puc_id", nullable = false)
  private Long cuentaPucId;

  @Column(nullable = false, length = 10)
  private String naturaleza;   // DEBITO | CREDITO

  @Column(nullable = false, precision = 19, scale = 6)
  private BigDecimal monto;

  @Column(length = 300)
  private String glosa;
}
