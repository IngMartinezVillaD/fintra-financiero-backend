package co.pluto.services.interfaces;

import co.pluto.dto.request.cupos.ActualizarCupoRotativoRequestDto;
import co.pluto.dto.request.cupos.CrearCupoRotativoRequestDto;
import co.pluto.dto.response.cupos.CupoRotativoResponseDto;

import java.util.List;

public interface ICupoRotativoService {
  CupoRotativoResponseDto crear(CrearCupoRotativoRequestDto request, String username);
  CupoRotativoResponseDto actualizar(Long id, ActualizarCupoRotativoRequestDto request);
  List<CupoRotativoResponseDto> listar();
  CupoRotativoResponseDto getById(Long id);
  List<CupoRotativoResponseDto> listarActivosPorEmpresa(Long empresaId);
}
