package co.fintra.financiero.dto.response.controles;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class PresuntoEmpresaDto {
  private Long empresaId;
  private String razonSocial;
  private Short anio;
  private BigDecimal totalPresuntoAnual;
  private List<PresuntoMensualItemDto> mensual;
}
