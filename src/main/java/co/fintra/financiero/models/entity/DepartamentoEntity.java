package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "catalogos", name = "departamentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartamentoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "codigo_dane", nullable = false, length = 2)
  private String codigoDane;

  @Column(nullable = false, length = 100)
  private String nombre;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pais_id", nullable = false)
  private PaisEntity pais;

  @Column(nullable = false)
  private Boolean activo = true;
}
