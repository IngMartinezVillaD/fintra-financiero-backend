package co.pluto.controllers;

import co.pluto.dto.request.puc.ActualizarPucRequestDto;
import co.pluto.dto.request.puc.CrearPucRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IPucService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/puc")
@RequiredArgsConstructor
@Tag(name = "Plan Único de Cuentas", description = "CRUD del PUC colombiano (Decreto 2649)")
public class PucController extends BaseController {

  private final IPucService pucService;

  @GetMapping
  @Operation(summary = "Listar todas las cuentas del PUC")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(pucService.listar());
  }

  @GetMapping("/buscar")
  @Operation(summary = "Buscar cuentas PUC por código o nombre")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> buscar(@RequestParam(required = false) String q) {
    return createSuccessResponse(pucService.buscar(q));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener una cuenta PUC por ID")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(pucService.obtener(id));
  }

  @PostMapping
  @Operation(summary = "Crear una nueva cuenta en el PUC")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> crear(@Valid @RequestBody CrearPucRequestDto request) {
    return createCustomResponse(pucService.crear(request), "Cuenta PUC creada exitosamente", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar una cuenta del PUC")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody ActualizarPucRequestDto request) {
    return createSuccessResponse(pucService.actualizar(id, request));
  }

  @PatchMapping("/{id}/activar")
  @Operation(summary = "Activar una cuenta del PUC")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> activar(@PathVariable Long id) {
    pucService.activar(id);
    return createSuccessResponse("Cuenta PUC activada");
  }

  @PatchMapping("/{id}/inactivar")
  @Operation(summary = "Inactivar una cuenta del PUC")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> inactivar(@PathVariable Long id) {
    pucService.inactivar(id);
    return createSuccessResponse("Cuenta PUC inactivada");
  }
}
