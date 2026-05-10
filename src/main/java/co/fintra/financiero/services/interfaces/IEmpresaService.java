package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.request.empresa.*;
import co.fintra.financiero.dto.response.empresa.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmpresaService {

  Page<EmpresaListItemDto> listar(String estado, String rolPermitido, String busqueda, Pageable pageable);
  EmpresaResponseDto obtener(Long id);
  EmpresaResponseDto crear(CrearEmpresaRequestDto request);
  EmpresaResponseDto actualizar(Long id, ActualizarEmpresaRequestDto request);
  void inactivar(Long id);

  CuentaBancariaResponseDto agregarCuentaBancaria(Long empresaId, CuentaBancariaRequestDto request);
  CuentaBancariaResponseDto editarCuentaBancaria(Long empresaId, Long cuentaId, CuentaBancariaRequestDto request);
  void desactivarCuentaBancaria(Long empresaId, Long cuentaId);

  TasaEspecialResponseDto solicitarTasaEspecial(Long empresaId, SolicitarTasaEspecialRequestDto request);
  TasaEspecialResponseDto aprobarTasaEspecial(Long empresaId, Long tasaId, String observacion);
  TasaEspecialResponseDto rechazarTasaEspecial(Long empresaId, Long tasaId, String observacion);
  List<TasaEspecialResponseDto> listarTasasEspeciales(Long empresaId);
}
