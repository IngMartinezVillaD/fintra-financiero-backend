package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "prestamos", name = "documentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String nombre;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "tamano_bytes", nullable = false)
  private Long tamanoBytes;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String url;

  @Column(length = 64)
  private String checksum;

  @Column(name = "subido_at", nullable = false)
  private OffsetDateTime subidoAt;

  @Column(name = "subido_por", nullable = false, length = 100)
  private String subidoPor;
}
