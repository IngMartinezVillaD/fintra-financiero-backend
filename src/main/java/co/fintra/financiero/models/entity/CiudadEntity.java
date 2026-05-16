package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "catalogos", name = "ciudades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CiudadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "codigo_dane", nullable = false, length = 5)
  private String codigoDane;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(name = "codigo_postal", length = 6)
  private String codigoPostal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "departamento_id", nullable = false)
  private DepartamentoEntity departamento;

  @Column(nullable = false)
  private Boolean activo = true;
}
