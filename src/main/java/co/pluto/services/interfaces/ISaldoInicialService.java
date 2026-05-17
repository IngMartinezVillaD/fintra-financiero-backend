package co.pluto.services.interfaces;

import co.pluto.dto.request.saldos.CrearSaldoInicialRequestDto;
import co.pluto.dto.response.saldos.SaldoInicialResponseDto;

import java.util.List;

public interface ISaldoInicialService {
  SaldoInicialResponseDto crear(CrearSaldoInicialRequestDto request, String username);
  List<SaldoInicialResponseDto> listar();
  SaldoInicialResponseDto getById(Long id);
}
