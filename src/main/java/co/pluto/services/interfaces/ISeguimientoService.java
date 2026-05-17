package co.pluto.services.interfaces;

import co.pluto.dto.request.abono.RegistrarAbonoRequestDto;
import co.pluto.dto.response.seguimiento.*;
import co.pluto.dto.response.operaciones.OperacionListItemDto;

import java.util.List;

public interface ISeguimientoService {

  List<OperacionListItemDto> listarVigentes();

  SeguimientoOperacionResponseDto obtenerSeguimiento(Long operacionId);

  SaldosSeparadosDto obtenerSaldos(Long operacionId);

  List<TramoDto> listarTramos(Long operacionId);

  List<AbonoDto> listarAbonos(Long operacionId);

  RegistrarAbonoResponseDto previewAbono(Long operacionId, RegistrarAbonoRequestDto request);

  RegistrarAbonoResponseDto registrarAbono(Long operacionId, RegistrarAbonoRequestDto request);
}
