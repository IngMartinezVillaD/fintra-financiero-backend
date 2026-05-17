package co.pluto.dto.response.operaciones;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class OperacionListItemDto {
  private Long id;
  private String referencia;
  private String empresaPrestamistaNombre;
  private String empresaPrestatariaNombre;
  private String cobraInteres;
  private BigDecimal montoEstimado;
  private String estadoPipeline;
  private LocalDate fechaCreacion;
  private String creadoPor;
  private Long diasEsperando;
}
