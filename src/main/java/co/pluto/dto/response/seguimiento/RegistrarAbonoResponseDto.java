package co.pluto.dto.response.seguimiento;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RegistrarAbonoResponseDto {
  private AbonoDto abono;
  private TramoDto tramoNuevo;
  private SaldosSeparadosDto saldosActuales;
  private boolean operacionSaldada;
}
