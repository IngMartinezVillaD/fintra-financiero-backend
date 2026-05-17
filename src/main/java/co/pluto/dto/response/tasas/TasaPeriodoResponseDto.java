package co.pluto.dto.response.tasas;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data @Builder
public class TasaPeriodoResponseDto {
  private Long id;
  private Short anio;
  private Short mes;
  private String tipoTasa;
  private BigDecimal valorPorcentajeEfectivoAnual;
  private BigDecimal valorPorcentajeMensual;
  private LocalDate vigenciaDesde;
  private LocalDate vigenciaHasta;
  private String estado;
  private String aprobadoPorNombre;
  private OffsetDateTime aprobadoAt;
  private String observacionAprobacion;
  private OffsetDateTime createdAt;
}
