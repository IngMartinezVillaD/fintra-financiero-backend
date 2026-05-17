package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "integraciones", name = "archivos_planos_bancarios")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivoPlanoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "banco_codigo", nullable = false, length = 20)
  private String bancoCodigo;

  @Column(nullable = false, length = 50)
  private String formato;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String contenido;

  @Column(name = "total_registros", nullable = false)
  private Integer totalRegistros = 0;

  @Column(name = "total_monto", nullable = false, precision = 19, scale = 6)
  private BigDecimal totalMonto = BigDecimal.ZERO;

  @Column(name = "fecha_generacion", nullable = false)
  private LocalDate fechaGeneracion;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;
}
