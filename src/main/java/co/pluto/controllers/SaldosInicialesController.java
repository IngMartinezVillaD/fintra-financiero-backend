package co.pluto.controllers;

import co.pluto.dto.request.saldos.CrearSaldoInicialRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.ISaldoInicialService;
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
@RequestMapping("/api/v1/saldos-iniciales")
@RequiredArgsConstructor
@Tag(name = "Saldos Iniciales", description = "Saldos de préstamos preexistentes al sistema")
public class SaldosInicialesController extends BaseController {

  private final ISaldoInicialService saldoService;

  @GetMapping
  @Operation(summary = "Listar todos los saldos iniciales")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(saldoService.listar());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener saldo inicial por ID")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> getById(@PathVariable Long id) {
    return createSuccessResponse(saldoService.getById(id));
  }

  @PostMapping
  @Operation(summary = "Registrar saldo inicial de préstamo preexistente")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> crear(
      @Valid @RequestBody CrearSaldoInicialRequestDto request,
      Principal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponseDto.builder()
            .code(HttpStatus.CREATED.value())
            .message("Saldo inicial registrado exitosamente")
            .data(saldoService.crear(request, principal.getName()))
            .build());
  }
}
