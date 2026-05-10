package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.request.tasas.RegistrarTasaRequestDto;
import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.services.interfaces.ITasaPeriodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasas-periodo")
@RequiredArgsConstructor
@Tag(name = "Tasas por Período", description = "Registro y aprobación de tasas generales del sistema")
public class TasaPeriodoController extends BaseController {

  private final ITasaPeriodoService tasaService;

  @GetMapping
  @Operation(summary = "Listar todas las tasas de período")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(tasaService.listar());
  }

  @GetMapping("/pendientes")
  @Operation(summary = "Listar tasas pendientes de aprobación")
  @PreAuthorize("hasAnyAuthority('ADMIN','APROBADOR')")
  public ResponseEntity<ApiResponseDto> listarPendientes() {
    return createSuccessResponse(tasaService.listarPendientes());
  }

  @GetMapping("/vigentes")
  @Operation(summary = "Obtener las 3 tasas vigentes al día de hoy")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> vigentes() {
    return createSuccessResponse(tasaService.listarVigentesHoy());
  }

  @PostMapping
  @Operation(summary = "Registrar nueva tasa de período")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> registrar(@Valid @RequestBody RegistrarTasaRequestDto request) {
    return createCustomResponse(tasaService.registrar(request), "Tasa registrada", HttpStatus.CREATED);
  }

  @PatchMapping("/{id}/aprobar")
  @Operation(summary = "Aprobar tasa de período")
  @PreAuthorize("hasAuthority('APROBADOR')")
  public ResponseEntity<ApiResponseDto> aprobar(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> body) {
    return createSuccessResponse(tasaService.aprobar(id, body != null ? body.get("observacion") : null));
  }

  @PatchMapping("/{id}/rechazar")
  @Operation(summary = "Rechazar tasa de período")
  @PreAuthorize("hasAuthority('APROBADOR')")
  public ResponseEntity<ApiResponseDto> rechazar(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {
    return createSuccessResponse(tasaService.rechazar(id, body.get("motivo")));
  }

  @GetMapping("/estado-bloqueo")
  @Operation(summary = "Evaluar estado de bloqueo global del sistema")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponseDto> estadoBloqueo() {
    return createSuccessResponse(tasaService.evaluarBloqueoSistema());
  }

  @GetMapping("/estado-bloqueo/empresas/{empresaId}")
  @Operation(summary = "Evaluar estado de bloqueo para una empresa específica")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR')")
  public ResponseEntity<ApiResponseDto> estadoBloqueoEmpresa(@PathVariable Long empresaId) {
    return createSuccessResponse(tasaService.evaluarBloqueoEmpresa(empresaId));
  }
}
