package co.pluto.controllers;

import co.pluto.dto.request.desembolso.ConfirmarDesembolsoRequestDto;
import co.pluto.dto.request.desembolso.GenerarArchivoPlanoRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IDesembolsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Tag(name = "Desembolsos", description = "Etapa DS — confirmación de desembolso y archivo plano bancario")
public class DesembolsoController extends BaseController {

  private final IDesembolsoService desembolsoService;

  @GetMapping("/api/v1/desembolsos/pendientes")
  @Operation(summary = "Operaciones en FD con firma completada, pendientes de desembolsar")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> listarPendientes() {
    return createSuccessResponse(desembolsoService.listarPendientes());
  }

  @GetMapping("/api/v1/operaciones/{id}/desembolsos/gmf-preview")
  @Operation(summary = "Calcula GMF estimado antes de confirmar")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> gmfPreview(
      @PathVariable Long id,
      @RequestParam BigDecimal monto) {
    return createSuccessResponse(desembolsoService.calcularGmfPreview(id, monto));
  }

  @PatchMapping("/api/v1/operaciones/{id}/desembolsar")
  @Operation(summary = "Confirma el desembolso: registra monto, GMF y abre tramo 1")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> confirmar(
      @PathVariable Long id,
      @Valid @RequestBody ConfirmarDesembolsoRequestDto request) {
    return createCustomResponse(desembolsoService.confirmar(id, request), "Desembolso confirmado", HttpStatus.OK);
  }

  @GetMapping("/api/v1/operaciones/{id}/desembolsos")
  @Operation(summary = "Lista desembolsos de una operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarPorOperacion(@PathVariable Long id) {
    return createSuccessResponse(desembolsoService.listarPorOperacion(id));
  }

  @GetMapping("/api/v1/operaciones/{id}/gmf")
  @Operation(summary = "Movimientos GMF de una operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> gmfMovimientos(@PathVariable Long id) {
    return createSuccessResponse(desembolsoService.listarPorOperacion(id)
        .stream().map(d -> new Object() {
          public final Boolean gmfAplica = d.getGmfAplica();
          public final java.math.BigDecimal gmfCalculado = d.getGmfCalculado();
          public final java.time.LocalDate fecha = d.getFecha();
        }).toList());
  }

  @PostMapping("/api/v1/desembolsos/archivo-plano")
  @Operation(summary = "Genera archivo plano bancario para una o varias operaciones (agrupadas por banco)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> generarArchivoPlano(
      @Valid @RequestBody GenerarArchivoPlanoRequestDto request) {
    return createCustomResponse(desembolsoService.generarArchivoPlano(request),
        "Archivos generados", HttpStatus.CREATED);
  }

  @GetMapping("/api/v1/desembolsos/archivos-planos/{archivoId}/descargar")
  @Operation(summary = "Descarga el contenido del archivo plano generado")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<byte[]> descargarArchivoPlano(@PathVariable Long archivoId) {
    String contenido = desembolsoService.obtenerContenidoArchivoPlano(archivoId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archivo-plano-" + archivoId + ".csv\"")
        .contentType(MediaType.TEXT_PLAIN)
        .body(contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
