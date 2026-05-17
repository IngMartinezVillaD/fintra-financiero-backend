package co.pluto.controllers;

import co.pluto.dto.request.interfaz.CrearInterfazContableRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IInterfazContableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interfaces-contables")
@RequiredArgsConstructor
@Tag(name = "Interfaces Contables", description = "Gestión de plantillas de interfaces contables por empresa y tipo de movimiento")
public class InterfazContableController extends BaseController {

  private final IInterfazContableService interfazService;

  @GetMapping
  @Operation(summary = "Listar todas las interfaces contables")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(interfazService.listar());
  }

  @GetMapping("/empresa/{empresaId}")
  @Operation(summary = "Listar interfaces contables por empresa")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarPorEmpresa(@PathVariable Long empresaId) {
    return createSuccessResponse(interfazService.listarPorEmpresa(empresaId));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener una interfaz contable por ID")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(interfazService.obtener(id));
  }

  @PostMapping
  @Operation(summary = "Crear nueva interfaz contable")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> crear(@Valid @RequestBody CrearInterfazContableRequestDto request) {
    return createCustomResponse(
        interfazService.crear(request), "Interfaz contable creada exitosamente", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar interfaz contable (reemplaza las líneas)")
  @PreAuthorize("hasAnyAuthority('ADMIN','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody CrearInterfazContableRequestDto request) {
    return createSuccessResponse(interfazService.actualizar(id, request));
  }

  @GetMapping("/tipos-movimiento")
  @Operation(summary = "Listar tipos de movimiento contable activos")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarTiposMovimiento() {
    return createSuccessResponse(interfazService.listarTiposMovimiento());
  }
}
