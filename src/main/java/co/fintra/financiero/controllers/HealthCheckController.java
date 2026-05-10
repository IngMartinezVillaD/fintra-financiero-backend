package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.services.interfaces.IHealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Estado del sistema")
public class HealthCheckController extends BaseController {

  private final IHealthCheckService healthCheckService;

  @GetMapping("/health-check")
  @Operation(summary = "Verifica el estado del sistema y la BD")
  public ResponseEntity<ApiResponseDto> healthCheck() {
    return createSuccessResponse(healthCheckService.check());
  }
}
