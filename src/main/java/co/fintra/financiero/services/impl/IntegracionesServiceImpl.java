package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.response.integraciones.IntegracionEstadoDto;
import co.fintra.financiero.dto.response.integraciones.NotificacionHistorialDto;
import co.fintra.financiero.models.entity.Bitrix24NotificacionEntity;
import co.fintra.financiero.models.repositories.IBitrix24NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntegracionesServiceImpl {

  @Value("${integraciones.bitrix24.activo:false}")
  private boolean bitrix24Activo;

  @Value("${integraciones.thomas-signe.base-url:}")
  private String thomasSigneUrl;

  @Value("${integraciones.apotheosys.activo:false}")
  private boolean apotheosysActivo;

  private final IBitrix24NotificacionRepository bitrix24Repo;

  public List<IntegracionEstadoDto> estado() {
    long exitosos  = bitrix24Repo.countEnviadasUltimas24h();
    long errores24 = bitrix24Repo.countErroresUltimas24h();

    String estadoBitrix;
    if (!bitrix24Activo)          estadoBitrix = "DESACTIVADO";
    else if (errores24 > exitosos && errores24 > 0) estadoBitrix = "DEGRADADO";
    else                          estadoBitrix = "OK";

    return List.of(
        IntegracionEstadoDto.builder()
            .nombre("Bitrix24").activo(bitrix24Activo).estado(estadoBitrix)
            .enviosExitosos24h(exitosos).errores24h(errores24)
            .ultimoMensaje(bitrix24Activo ? null : "Feature flag desactivado").build(),

        IntegracionEstadoDto.builder()
            .nombre("Thomas Signe").activo(true).estado("OK")
            .enviosExitosos24h(0).errores24h(0)
            .ultimoMensaje("Mock activo — spec real pendiente").build(),

        IntegracionEstadoDto.builder()
            .nombre("Apotheosys ERP").activo(apotheosysActivo).estado("DESACTIVADO")
            .enviosExitosos24h(0).errores24h(0)
            .ultimoMensaje("Spec API pendiente de Contabilidad").build(),

        IntegracionEstadoDto.builder()
            .nombre("SIIGO ERP").activo(false).estado("DESACTIVADO")
            .enviosExitosos24h(0).errores24h(0)
            .ultimoMensaje("Spec plantilla pendiente de Contabilidad").build()
    );
  }

  public List<NotificacionHistorialDto> historialBitrix24(int page, int size) {
    return bitrix24Repo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
        .stream()
        .map(this::toHistorialDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void reenviar(Long notificacionId) {
    Bitrix24NotificacionEntity n = bitrix24Repo.findById(notificacionId)
        .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
    n.setEstado("PENDIENTE");
    n.setReintentos((short) 0);
    n.setUltimoError(null);
    bitrix24Repo.save(n);
  }

  private NotificacionHistorialDto toHistorialDto(Bitrix24NotificacionEntity n) {
    return NotificacionHistorialDto.builder()
        .id(n.getId()).eventoCodigo(n.getEventoCodigo()).estado(n.getEstado())
        .reintentos(n.getReintentos()).ultimoError(n.getUltimoError())
        .createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt())
        .build();
  }
}
