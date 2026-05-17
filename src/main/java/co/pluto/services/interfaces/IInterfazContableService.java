package co.pluto.services.interfaces;

import co.pluto.dto.request.interfaz.CrearInterfazContableRequestDto;
import co.pluto.dto.response.interfaz.InterfazContableResponseDto;
import co.pluto.dto.response.interfaz.TipoMovimientoContableDto;

import java.util.List;

public interface IInterfazContableService {

  List<InterfazContableResponseDto> listar();

  List<InterfazContableResponseDto> listarPorEmpresa(Long empresaId);

  InterfazContableResponseDto obtener(Long id);

  InterfazContableResponseDto crear(CrearInterfazContableRequestDto req);

  InterfazContableResponseDto actualizar(Long id, CrearInterfazContableRequestDto req);

  List<TipoMovimientoContableDto> listarTiposMovimiento();
}
