package co.fintra.financiero.dto.response.liquidacion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder
public class LiquidacionMensualResponseDto {
  private Long id;
  private Short anio;
  private Short mes;
  private String periodo;
  private LocalDate fechaCorte;
  private String estado;
  private BigDecimal totalInteresesLiquidados;
  private BigDecimal totalRetencionFuente;
  private BigDecimal totalRetencionIca;
  private BigDecimal totalNetoCobrar;
  private String aprobadaPorNombre;
  private OffsetDateTime aprobadaAt;
  private OffsetDateTime createdAt;
  private List<LiquidacionDetalleItemDto> detalle;
}
