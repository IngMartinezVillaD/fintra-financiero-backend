package co.fintra.financiero.dto.response.empresa;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data @Builder
public class TasaEspecialResponseDto {
  private Long id;
  private BigDecimal valorPorcentajeEfectivoAnual;
  private BigDecimal valorPorcentajeMensual;
  private LocalDate vigenciaDesde;
  private LocalDate vigenciaHasta;
  private String estado;
  private String aprobadoPorNombre;
  private OffsetDateTime aprobadoAt;
  private String observacion;
  private OffsetDateTime createdAt;
}
