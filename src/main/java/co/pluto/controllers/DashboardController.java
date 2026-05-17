package co.pluto.controllers;

import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Consolidado financiero, cartera e indicadores")
public class DashboardController extends BaseController {

  private final IDashboardService dashboardService;

  @GetMapping
  @Operation(summary = "Dashboard completo: cartera, consolidado, alertas, tasas vigentes")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> dashboard() {
    return createSuccessResponse(dashboardService.dashboard());
  }

  @GetMapping("/consolidado")
  @Operation(summary = "Consolidado financiero por empresa — solo operaciones DS")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> consolidado() {
    return createSuccessResponse(dashboardService.consolidadoFinanciero());
  }

  @GetMapping("/evolucion")
  @Operation(summary = "Evolución mensual de saldos e intereses liquidados")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> evolucion(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      @RequestParam(required = false) Long empresaId) {
    return createSuccessResponse(dashboardService.evolucionMensual(desde, hasta, empresaId));
  }

  @GetMapping("/kpis-gerenciales")
  @Operation(summary = "KPIs gerenciales: días promedio aprobación, rechazadas, tasa ponderada")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR')")
  public ResponseEntity<ApiResponseDto> kpis() {
    return createSuccessResponse(dashboardService.kpisGerenciales());
  }
}
