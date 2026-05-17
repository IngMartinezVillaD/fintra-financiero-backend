package co.pluto.controllers;

import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.impl.ContabilizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/asientos-contables")
@RequiredArgsConstructor
@Tag(name = "Asientos Contables", description = "Consulta de asientos generados por el motor de contabilización")
public class AsientosContablesController extends BaseController {

  private final ContabilizacionService contabilizacionService;

  @GetMapping
  @Operation(summary = "Listar asientos por origen (tipoOrigen=LIQUIDACION|DESEMBOLSO, origenId=id)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar(
      @RequestParam String tipoOrigen,
      @RequestParam Long   origenId) {
    return createSuccessResponse(contabilizacionService.listarPorOrigen(tipoOrigen, origenId));
  }

  @GetMapping("/buscar")
  @Operation(summary = "Consulta general de asientos con filtros opcionales")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> buscar(
      @RequestParam(required = false) String    tipoOrigen,
      @RequestParam(required = false) Long      empresaId,
      @RequestParam(required = false) String    estado,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
    return createSuccessResponse(
        contabilizacionService.buscar(tipoOrigen, empresaId, estado, fechaDesde, fechaHasta));
  }
}
