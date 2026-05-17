package co.pluto.controllers;

import co.pluto.dto.request.operaciones.CrearOperacionRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IOperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/operaciones")
@RequiredArgsConstructor
@Tag(name = "Operaciones", description = "Cartera CR→AI→AE→FD→DS de préstamos intercompañía")
public class OperacionController extends BaseController {

  private final IOperacionService operacionService;

  @GetMapping
  @Operation(summary = "Listar operaciones con filtros y paginación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','EMPRESA_RECEPTORA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar(
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) Long prestamistaId,
      @RequestParam(required = false) Long prestatariaId,
      @RequestParam(required = false) String referencia,
      @RequestParam(defaultValue = "0")  int page,
      @RequestParam(defaultValue = "10") int size) {
    return createSuccessResponse(
        operacionService.listar(estado, prestamistaId, prestatariaId, referencia,
            PageRequest.of(page, size)));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener detalle de operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','EMPRESA_RECEPTORA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(operacionService.obtener(id));
  }

  @PostMapping
  @Operation(summary = "Crear nueva operación (etapa CR)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> crear(@Valid @RequestBody CrearOperacionRequestDto request) {
    return createCustomResponse(operacionService.crear(request), "Operación creada", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Editar operación (solo estado CR)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> editar(
      @PathVariable Long id,
      @Valid @RequestBody CrearOperacionRequestDto request) {
    return createSuccessResponse(operacionService.editar(id, request));
  }

  @PatchMapping("/{id}/cancelar")
  @Operation(summary = "Cancelar operación (CR o AI)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> cancelar(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    String motivo = body != null ? body.get("motivo") : null;
    return createSuccessResponse(operacionService.cancelar(id, motivo));
  }

  @PatchMapping("/{id}/enviar-aprobacion")
  @Operation(summary = "Enviar operación a aprobación interna (CR → AI)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> enviarAprobacion(@PathVariable Long id) {
    return createSuccessResponse(operacionService.enviarAprobacion(id));
  }

  @GetMapping("/aviso-tramo-anterior/{empresaPrestatariaId}")
  @Operation(summary = "Calcular aviso de tramo anterior activo para una empresa prestataria")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> avisoTramoAnterior(@PathVariable Long empresaPrestatariaId) {
    return createSuccessResponse(
        operacionService.calcularAvisoTramoAnterior(empresaPrestatariaId).orElse(null));
  }

  // ── AI: Aprobación interna ──────────────────────────────────────

  @GetMapping("/pendientes-aprobacion")
  @Operation(summary = "Bandeja del Aprobador — operaciones en estado AI")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> pendientesAprobacion() {
    return createSuccessResponse(operacionService.listarPendientesAprobacion());
  }

  @PatchMapping("/{id}/aprobar-interna")
  @Operation(summary = "Aprobar operación internamente (AI → AE)")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> aprobarInterna(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    return createSuccessResponse(
        operacionService.aprobarInterna(id, body != null ? body.get("observacion") : null));
  }

  @PatchMapping("/{id}/devolver")
  @Operation(summary = "Devolver operación al creador (AI → CR)")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> devolverDesdeAI(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {
    return createSuccessResponse(operacionService.devolverDesdeAI(id, body.get("observacion")));
  }

  @PatchMapping("/{id}/rechazar-interna")
  @Operation(summary = "Rechazar operación en etapa AI")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> rechazarInterna(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {
    return createSuccessResponse(operacionService.rechazarInterna(id, body.get("motivo")));
  }

  // ── AE: Aceptación empresa ──────────────────────────────────────

  @GetMapping("/pendientes-aceptacion")
  @Operation(summary = "Bandeja empresa — operaciones en estado AE de sus empresas")
  @PreAuthorize("hasAnyAuthority('EMPRESA_RECEPTORA','ADMIN')")
  public ResponseEntity<ApiResponseDto> pendientesAceptacion() {
    return createSuccessResponse(operacionService.listarPendientesAceptacion());
  }

  @PatchMapping("/{id}/aceptar-empresa")
  @Operation(summary = "Aceptar operación como empresa prestataria (AE → FD)")
  @PreAuthorize("hasAnyAuthority('EMPRESA_RECEPTORA','ADMIN')")
  public ResponseEntity<ApiResponseDto> aceptarEmpresa(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    return createSuccessResponse(
        operacionService.aceptarEmpresa(id, body != null ? body.get("observacion") : null));
  }

  @PatchMapping("/{id}/rechazar-empresa")
  @Operation(summary = "Rechazar operación como empresa prestataria (AE → RECHAZADA)")
  @PreAuthorize("hasAnyAuthority('EMPRESA_RECEPTORA','ADMIN')")
  public ResponseEntity<ApiResponseDto> rechazarEmpresa(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {
    return createSuccessResponse(operacionService.rechazarEmpresa(id, body.get("motivo")));
  }

  @GetMapping("/{id}/historial")
  @Operation(summary = "Historial completo de eventos de la operación")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponseDto> historial(@PathVariable Long id) {
    return createSuccessResponse(operacionService.historial(id));
  }
}
