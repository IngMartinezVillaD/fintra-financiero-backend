package co.pluto.controllers;

import co.pluto.dto.request.abono.RegistrarAbonoRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.ISeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Seguimiento", description = "Etapa DS — tramos, abonos y saldos separados")
public class SeguimientoController extends BaseController {

  private final ISeguimientoService seguimientoService;

  @GetMapping("/api/v1/operaciones/seguimiento")
  @Operation(summary = "Lista todas las operaciones activas en estado DS")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarVigentes() {
    return createSuccessResponse(seguimientoService.listarVigentes());
  }

  @GetMapping("/api/v1/operaciones/{id}/seguimiento")
  @Operation(summary = "Snapshot completo de seguimiento: saldos, tramos y abonos")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(seguimientoService.obtenerSeguimiento(id));
  }

  @GetMapping("/api/v1/operaciones/{id}/saldos")
  @Operation(summary = "Saldos separados en vivo (capital, intereses, GMF, interés en curso)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> saldos(@PathVariable Long id) {
    return createSuccessResponse(seguimientoService.obtenerSaldos(id));
  }

  @GetMapping("/api/v1/operaciones/{id}/tramos")
  @Operation(summary = "Historial de tramos de una operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> tramos(@PathVariable Long id) {
    return createSuccessResponse(seguimientoService.listarTramos(id));
  }

  @GetMapping("/api/v1/operaciones/{id}/abonos")
  @Operation(summary = "Historial de abonos de una operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> abonos(@PathVariable Long id) {
    return createSuccessResponse(seguimientoService.listarAbonos(id));
  }

  @PostMapping("/api/v1/operaciones/{id}/abonos/preview")
  @Operation(summary = "Simula un abono sin persistir — muestra cómo se aplica el pago")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> previewAbono(
      @PathVariable Long id,
      @Valid @RequestBody RegistrarAbonoRequestDto request) {
    return createSuccessResponse(seguimientoService.previewAbono(id, request));
  }

  @PostMapping("/api/v1/operaciones/{id}/abonos")
  @Operation(summary = "Registra un abono: liquida tramo previo, aplica pago y abre tramo nuevo")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> registrar(
      @PathVariable Long id,
      @Valid @RequestBody RegistrarAbonoRequestDto request) {
    return createCustomResponse(
        seguimientoService.registrarAbono(id, request), "Abono registrado", HttpStatus.CREATED);
  }
}
