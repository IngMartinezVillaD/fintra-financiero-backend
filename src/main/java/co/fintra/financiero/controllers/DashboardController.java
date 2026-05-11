package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.services.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Consolidado financiero e indicadores")
public class DashboardController extends BaseController {

  private final IDashboardService dashboardService;

  @GetMapping("/consolidado")
  @Operation(summary = "Consolidado financiero por empresa — solo operaciones DS")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> consolidado() {
    return createSuccessResponse(dashboardService.consolidadoFinanciero());
  }
}
