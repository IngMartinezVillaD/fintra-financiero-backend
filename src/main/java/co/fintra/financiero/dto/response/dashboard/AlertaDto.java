package co.fintra.financiero.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data @Builder
public class AlertaDto {
  private String tipo;
  private String subtipo;
  private Long empresaId;
  private String empresaRazonSocial;
  private LocalDate fechaVigenciaHasta;
  private Integer diasRestantes;
  private String estado;
}
