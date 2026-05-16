package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "catalogos", name = "paises")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaisEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "codigo_iso2", nullable = false, length = 2)
  private String codigoIso2;

  @Column(name = "codigo_iso3", nullable = false, length = 3)
  private String codigoIso3;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false)
  private Boolean activo = true;
}
