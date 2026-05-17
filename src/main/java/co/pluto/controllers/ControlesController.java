package co.pluto.controllers;

import co.pluto.dto.request.controles.DecisionAnualGmfRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IControlesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/controles")
@RequiredArgsConstructor
@Tag(name = "Controles Extracontables", description = "GMF e interés presunto fiscal — nunca generan asientos contables")
public class ControlesController extends BaseController {

  private final IControlesService controlesService;

  // ── GMF ─────────────────────────────────────────────────────────

  @GetMapping("/gmf")
  @Operation(summary = "Consolidado GMF por empresa para un año")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> consolidadoGmf(@RequestParam Short anio) {
    return createSuccessResponse(controlesService.consolidadoGmf(anio));
  }

  @GetMapping("/gmf/empresas/{id}/{anio}")
  @Operation(summary = "Detalle GMF por empresa y año (movimientos mensuales)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> gmfPorEmpresa(@PathVariable Long id, @PathVariable Short anio) {
    return createSuccessResponse(controlesService.gmfPorEmpresa(id, anio));
  }

  @PostMapping("/gmf/decisiones-anuales")
  @Operation(summary = "Registrar decisión anual GMF (COBRAR/ASUMIR) — irreversible")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> registrarDecision(@Valid @RequestBody DecisionAnualGmfRequestDto req) {
    controlesService.registrarDecisionAnualGmf(req);
    return createSuccessResponse("Decisión registrada correctamente");
  }

  // ── Presunto ────────────────────────────────────────────────────

  @GetMapping("/presunto")
  @Operation(summary = "Consolidado interés presunto por empresa para un año")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> consolidadoPresunto(@RequestParam Short anio) {
    return createSuccessResponse(controlesService.consolidadoPresunto(anio));
  }

  @GetMapping("/presunto/empresas/{id}/{anio}")
  @Operation(summary = "Presunto mensual + anual por empresa")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> presuntoPorEmpresa(@PathVariable Long id, @PathVariable Short anio) {
    return createSuccessResponse(controlesService.presuntoPorEmpresa(id, anio));
  }

  @PostMapping("/presunto/{anio}/{mes}/ejecutar")
  @Operation(summary = "Ejecutar cálculo de presunto para un mes/año (idempotente)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> ejecutarPresunto(@PathVariable Short anio, @PathVariable Short mes) {
    int procesados = controlesService.ejecutarPresuntoMensual(anio, mes);
    return createSuccessResponse("Presunto ejecutado: " + procesados + " registros generados");
  }
}
