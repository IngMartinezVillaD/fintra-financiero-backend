package co.pluto.dto.response.liquidacion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class LiquidacionDiariaResponseDto {
  private Long id;
  private LocalDate fecha;
  private String periodo;
  private String estado;
  private BigDecimal totalInteresesLiquidados;
  private BigDecimal totalRetencionFuente;
  private BigDecimal totalRetencionIca;
  private BigDecimal totalNetoCobrar;
  private String aprobadaPorNombre;
  private OffsetDateTime aprobadaAt;
  private OffsetDateTime createdAt;
  private List<LiquidacionDiariaDetalleItemDto> detalle;
}
