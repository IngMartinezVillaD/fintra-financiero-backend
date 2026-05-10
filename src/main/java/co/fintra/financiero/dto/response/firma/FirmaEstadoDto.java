package co.fintra.financiero.dto.response.firma;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class FirmaEstadoDto {
  private UUID solicitudId;
  private Long operacionId;
  private String estado;
  private String destinatarioEmail;
  private String documentoUrl;
  private OffsetDateTime enviadoAt;
  private OffsetDateTime firmadoAt;
  private OffsetDateTime createdAt;
}
