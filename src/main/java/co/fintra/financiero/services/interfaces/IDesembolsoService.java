package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.request.desembolso.ConfirmarDesembolsoRequestDto;
import co.fintra.financiero.dto.request.desembolso.GenerarArchivoPlanoRequestDto;
import co.fintra.financiero.dto.response.desembolso.ArchivoPlanoResponseDto;
import co.fintra.financiero.dto.response.desembolso.DesembolsoResponseDto;
import co.fintra.financiero.dto.response.desembolso.GmfResumenDto;
import co.fintra.financiero.dto.response.operaciones.OperacionListItemDto;

import java.util.List;

public interface IDesembolsoService {

  List<OperacionListItemDto> listarPendientes();

  GmfResumenDto calcularGmfPreview(Long operacionId, java.math.BigDecimal monto);

  DesembolsoResponseDto confirmar(Long operacionId, ConfirmarDesembolsoRequestDto request);

  List<DesembolsoResponseDto> listarPorOperacion(Long operacionId);

  List<ArchivoPlanoResponseDto> generarArchivoPlano(GenerarArchivoPlanoRequestDto request);

  String obtenerContenidoArchivoPlano(Long archivoPlanoId);
}
