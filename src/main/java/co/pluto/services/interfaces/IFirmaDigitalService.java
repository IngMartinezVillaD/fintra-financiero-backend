package co.pluto.services.interfaces;

import co.pluto.dto.response.firma.FirmaEstadoDto;

import java.util.Optional;

public interface IFirmaDigitalService {

  FirmaEstadoDto iniciarFirma(Long operacionId);

  FirmaEstadoDto reenviarFirma(Long operacionId);

  Optional<FirmaEstadoDto> consultarEstado(Long operacionId);

  void procesarWebhook(String payload, String signature);

  FirmaEstadoDto marcarFirmadaManual(Long operacionId);
}
