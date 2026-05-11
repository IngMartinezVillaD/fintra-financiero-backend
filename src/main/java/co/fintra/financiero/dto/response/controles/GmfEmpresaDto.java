package co.fintra.financiero.dto.response.controles;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class GmfEmpresaDto {
  private Long empresaId;
  private String razonSocial;
  private Short anio;
  private BigDecimal totalGmf;
  private String decisionAnual;
  private List<GmfMovimientoItemDto> movimientos;
}
