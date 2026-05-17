package co.pluto.services.interfaces;

import co.pluto.dto.request.liquidacion.EjecutarRangoDiarioRequestDto;
import co.pluto.dto.request.liquidacion.IniciarLiquidacionDiariaRequestDto;
import co.pluto.dto.response.liquidacion.LiquidacionDiariaResponseDto;
import co.pluto.dto.response.liquidacion.RangoLiquidacionDiariaResponseDto;

import java.util.List;

public interface ILiquidacionDiariaService {

  LiquidacionDiariaResponseDto iniciar(IniciarLiquidacionDiariaRequestDto request);

  LiquidacionDiariaResponseDto calcular(Long id);

  RangoLiquidacionDiariaResponseDto ejecutarRango(EjecutarRangoDiarioRequestDto req);

  List<LiquidacionDiariaResponseDto> listar();

  LiquidacionDiariaResponseDto obtener(Long id);

  LiquidacionDiariaResponseDto aprobar(Long id);

  LiquidacionDiariaResponseDto revertir(Long id);

  LiquidacionDiariaResponseDto marcarContabilizada(Long id);
}
