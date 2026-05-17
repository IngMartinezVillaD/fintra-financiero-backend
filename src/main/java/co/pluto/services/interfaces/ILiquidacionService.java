package co.pluto.services.interfaces;

import co.pluto.dto.request.liquidacion.IniciarLiquidacionRequestDto;
import co.pluto.dto.response.liquidacion.LiquidacionMensualResponseDto;

import java.util.List;

public interface ILiquidacionService {

  LiquidacionMensualResponseDto iniciar(IniciarLiquidacionRequestDto request);

  LiquidacionMensualResponseDto calcular(Long id);

  List<LiquidacionMensualResponseDto> listar();

  LiquidacionMensualResponseDto obtener(Long id);

  LiquidacionMensualResponseDto aprobar(Long id);

  LiquidacionMensualResponseDto revertir(Long id);

  LiquidacionMensualResponseDto marcarContabilizada(Long id);

  byte[] descargarPlantilla(Long id, Long empresaId);
}
