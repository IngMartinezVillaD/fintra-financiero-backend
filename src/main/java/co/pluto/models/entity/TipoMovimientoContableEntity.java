package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "tipos_movimiento_contable")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoMovimientoContableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 60)
  private String codigo;

  @Column(nullable = false, length = 200)
  private String nombre;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Column(nullable = false)
  private Boolean activo = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
