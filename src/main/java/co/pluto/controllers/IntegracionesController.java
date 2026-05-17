package co.pluto.controllers;

import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.impl.IntegracionesServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integraciones")
@RequiredArgsConstructor
@Tag(name = "Integraciones", description = "Estado y historial de integraciones ERP y notificaciones")
public class IntegracionesController extends BaseController {

  private final IntegracionesServiceImpl integracionesService;

  @GetMapping("/estado")
  @Operation(summary = "Estado de todas las integraciones (Bitrix24, Thomas Signe, Apotheosys, SIIGO)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> estado() {
    return createSuccessResponse(integracionesService.estado());
  }

  @GetMapping("/bitrix24/historial")
  @Operation(summary = "Historial de notificaciones Bitrix24")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> historialBitrix24(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return createSuccessResponse(integracionesService.historialBitrix24(page, size));
  }

  @PostMapping("/bitrix24/{id}/reenviar")
  @Operation(summary = "Marcar notificación fallida para reenvío")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> reenviar(@PathVariable Long id) {
    integracionesService.reenviar(id);
    return createSuccessResponse("Notificación marcada para reenvío");
  }
}
