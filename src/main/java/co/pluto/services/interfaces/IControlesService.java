package co.pluto.services.interfaces;

import co.pluto.dto.request.controles.DecisionAnualGmfRequestDto;
import co.pluto.dto.response.controles.*;

import java.util.List;

public interface IControlesService {

  // GMF
  List<GmfEmpresaDto> consolidadoGmf(Short anio);
  GmfEmpresaDto gmfPorEmpresa(Long empresaId, Short anio);
  void registrarDecisionAnualGmf(DecisionAnualGmfRequestDto request);

  // Presunto
  List<PresuntoEmpresaDto> consolidadoPresunto(Short anio);
  PresuntoEmpresaDto presuntoPorEmpresa(Long empresaId, Short anio);
  int ejecutarPresuntoMensual(Short anio, Short mes);
}
