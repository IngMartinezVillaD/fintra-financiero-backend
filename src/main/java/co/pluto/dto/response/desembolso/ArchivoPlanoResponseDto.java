package co.pluto.dto.response.desembolso;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class ArchivoPlanoResponseDto {
  private Long id;
  private String bancoCodigo;
  private String bancoNombre;
  private String formato;
  private Integer totalRegistros;
  private BigDecimal totalMonto;
  private LocalDate fechaGeneracion;
  private List<Long> operacionIds;
  private String urlDescarga;
}
