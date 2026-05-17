package co.pluto.services.interfaces;

import co.pluto.dto.request.puc.ActualizarPucRequestDto;
import co.pluto.dto.request.puc.CrearPucRequestDto;
import co.pluto.dto.response.puc.PucResponseDto;

import java.util.List;

public interface IPucService {

  List<PucResponseDto> listar();

  List<PucResponseDto> buscar(String q);

  PucResponseDto obtener(Long id);

  PucResponseDto crear(CrearPucRequestDto req);

  PucResponseDto actualizar(Long id, ActualizarPucRequestDto req);

  void activar(Long id);

  void inactivar(Long id);
}
