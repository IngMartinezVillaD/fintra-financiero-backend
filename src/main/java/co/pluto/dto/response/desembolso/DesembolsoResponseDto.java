package co.pluto.dto.response.desembolso;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data @Builder
public class DesembolsoResponseDto {
  private Long id;
  private Long operacionId;
  private String referencia;
  private BigDecimal monto;
  private LocalDate fecha;
  private Boolean gmfAplica;
  private BigDecimal gmfCalculado;
  private Long archivoPlanoId;
  private OffsetDateTime createdAt;
  private TramoInicialDto tramoInicial;
}
