package co.pluto.dto.response.controles;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class GmfMovimientoItemDto {
  private Long id;
  private Long operacionId;
  private String referencia;
  private Short anio;
  private Short mes;
  private LocalDate fecha;
  private BigDecimal montoGmf;
  private String decisionAnual;
}
