package co.pluto.dto.response.liquidacion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RangoLiquidacionDiariaResponseDto {
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;
  private int totalDias;
  private int creadas;
  private int omitidas;
  private List<LiquidacionDiariaResponseDto> liquidaciones;
}
