package co.pluto.services.interfaces;

import co.pluto.dto.request.tasas.RegistrarTasaRequestDto;
import co.pluto.dto.response.tasas.EstadoBloqueoDto;
import co.pluto.dto.response.tasas.TasaPeriodoResponseDto;

import java.util.List;

public interface ITasaPeriodoService {

  List<TasaPeriodoResponseDto> listar();
  List<TasaPeriodoResponseDto> listarPendientes();
  List<TasaPeriodoResponseDto> listarVigentesHoy();

  TasaPeriodoResponseDto registrar(RegistrarTasaRequestDto request);
  TasaPeriodoResponseDto aprobar(Long id, String observacion);
  TasaPeriodoResponseDto rechazar(Long id, String motivo);

  EstadoBloqueoDto evaluarBloqueoSistema();
  EstadoBloqueoDto evaluarBloqueoEmpresa(Long empresaId);
}
