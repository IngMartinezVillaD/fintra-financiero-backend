package co.pluto.controllers;

import co.pluto.dto.request.cupos.ActualizarCupoRotativoRequestDto;
import co.pluto.dto.request.cupos.CrearCupoRotativoRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.ICupoRotativoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/cupos-rotativos")
@RequiredArgsConstructor
@Tag(name = "Cupos Rotativos", description = "Gestión de líneas de crédito rotativo por empresa")
public class CuposRotativosController extends BaseController {

  private final ICupoRotativoService cupoService;

  @GetMapping
  @Operation(summary = "Listar todos los cupos rotativos")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(cupoService.listar());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener cupo rotativo por ID")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> getById(@PathVariable Long id) {
    return createSuccessResponse(cupoService.getById(id));
  }

  @GetMapping("/empresa/{empresaId}/activos")
  @Operation(summary = "Cupos rotativos activos de una empresa prestataria")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> activosPorEmpresa(@PathVariable Long empresaId) {
    return createSuccessResponse(cupoService.listarActivosPorEmpresa(empresaId));
  }

  @PostMapping
  @Operation(summary = "Crear nuevo cupo rotativo")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> crear(
      @Valid @RequestBody CrearCupoRotativoRequestDto request,
      Principal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponseDto.builder()
            .code(HttpStatus.CREATED.value())
            .message("Cupo rotativo creado exitosamente")
            .data(cupoService.crear(request, principal.getName()))
            .build());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar valor, estado y observaciones del cupo rotativo")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody ActualizarCupoRotativoRequestDto request) {
    return createSuccessResponse(cupoService.actualizar(id, request));
  }
}
