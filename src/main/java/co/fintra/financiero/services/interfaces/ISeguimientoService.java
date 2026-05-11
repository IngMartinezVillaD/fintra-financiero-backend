package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.request.abono.RegistrarAbonoRequestDto;
import co.fintra.financiero.dto.response.seguimiento.*;
import co.fintra.financiero.dto.response.operaciones.OperacionListItemDto;

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
