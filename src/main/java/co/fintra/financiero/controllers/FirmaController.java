package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.services.interfaces.IFirmaDigitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operaciones/{id}/firma")
@RequiredArgsConstructor
@Tag(name = "Firma Digital", description = "Gestión de la etapa FD — integración Thomas Signe")
public class FirmaController extends BaseController {

  private final IFirmaDigitalService firmaService;

  @PostMapping("/iniciar")
  @Operation(summary = "Iniciar proceso de firma digital (FD)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> iniciar(@PathVariable Long id) {
    return createCustomResponse(firmaService.iniciarFirma(id), "Firma iniciada", HttpStatus.CREATED);
  }

  @PostMapping("/reenviar")
  @Operation(summary = "Reenviar link de firma cuando expiró o falló")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> reenviar(@PathVariable Long id) {
    return createSuccessResponse(firmaService.reenviarFirma(id));
  }

  @GetMapping
  @Operation(summary = "Consultar estado actual de la firma digital")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> estado(@PathVariable Long id) {
    return createSuccessResponse(firmaService.consultarEstado(id).orElse(null));
  }
}
