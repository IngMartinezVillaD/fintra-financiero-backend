package co.pluto.dto.response.seguimiento;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class SeguimientoOperacionResponseDto {
  private Long id;
  private String referencia;
  private String empresaPrestamistaNombre;
  private String empresaPrestatariaNombre;
  private String cobraInteres;
  private LocalDate fechaDesembolso;
  private BigDecimal montoDesembolsado;
  private SaldosSeparadosDto saldos;
  private List<TramoDto> tramos;
  private List<AbonoDto> abonos;
}
