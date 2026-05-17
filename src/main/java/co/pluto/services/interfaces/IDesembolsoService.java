package co.pluto.services.interfaces;

import co.pluto.dto.request.desembolso.ConfirmarDesembolsoRequestDto;
import co.pluto.dto.request.desembolso.GenerarArchivoPlanoRequestDto;
import co.pluto.dto.response.desembolso.ArchivoPlanoResponseDto;
import co.pluto.dto.response.desembolso.DesembolsoResponseDto;
import co.pluto.dto.response.desembolso.GmfResumenDto;
import co.pluto.dto.response.operaciones.OperacionListItemDto;

import java.util.List;

public interface IDesembolsoService {

  List<OperacionListItemDto> listarPendientes();

  GmfResumenDto calcularGmfPreview(Long operacionId, java.math.BigDecimal monto);

  DesembolsoResponseDto confirmar(Long operacionId, ConfirmarDesembolsoRequestDto request);

  List<DesembolsoResponseDto> listarPorOperacion(Long operacionId);

  List<ArchivoPlanoResponseDto> generarArchivoPlano(GenerarArchivoPlanoRequestDto request);

  String obtenerContenidoArchivoPlano(Long archivoPlanoId);
}
