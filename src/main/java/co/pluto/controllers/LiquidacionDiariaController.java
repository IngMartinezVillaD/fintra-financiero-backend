package co.pluto.controllers;

import co.pluto.dto.request.liquidacion.EjecutarRangoDiarioRequestDto;
import co.pluto.dto.request.liquidacion.IniciarLiquidacionDiariaRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.ILiquidacionDiariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/liquidaciones-diarias")
@RequiredArgsConstructor
@Tag(name = "Liquidación Diaria", description = "Causación diaria de intereses sin cierre de tramos")
public class LiquidacionDiariaController extends BaseController {

  private final ILiquidacionDiariaService liquidacionDiariaService;

  @PostMapping
  @Operation(summary = "Iniciar liquidación diaria (crea en estado BORRADOR)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> iniciar(@Valid @RequestBody IniciarLiquidacionDiariaRequestDto req) {
    return createCustomResponse(liquidacionDiariaService.iniciar(req), "Liquidación diaria iniciada", HttpStatus.CREATED);
  }

  @PostMapping("/rango")
  @Operation(summary = "Ejecutar liquidación diaria para un rango de fechas — crea y calcula cada día, omite los que ya existen")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> ejecutarRango(@Valid @RequestBody EjecutarRangoDiarioRequestDto req) {
    return createSuccessResponse(liquidacionDiariaService.ejecutarRango(req));
  }

  @PostMapping("/{id}/calcular")
  @Operation(summary = "Ejecutar motor de cálculo diario → pasa a PENDIENTE_APROBACION")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> calcular(@PathVariable Long id) {
    return createSuccessResponse(liquidacionDiariaService.calcular(id));
  }

  @GetMapping
  @Operation(summary = "Listar liquidaciones diarias")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(liquidacionDiariaService.listar());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalle completo de la liquidación diaria con línea por operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(liquidacionDiariaService.obtener(id));
  }

  @PatchMapping("/{id}/aprobar")
  @Operation(summary = "Aprobar liquidación diaria (PENDIENTE_APROBACION → APROBADA)")
  @PreAuthorize("hasAnyAuthority('ADMIN','APROBADOR')")
  public ResponseEntity<ApiResponseDto> aprobar(@PathVariable Long id) {
    return createSuccessResponse(liquidacionDiariaService.aprobar(id));
  }

  @PatchMapping("/{id}/revertir")
  @Operation(summary = "Revertir liquidación diaria a BORRADOR (solo en BORRADOR o PENDIENTE_APROBACION)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> revertir(@PathVariable Long id) {
    return createSuccessResponse(liquidacionDiariaService.revertir(id));
  }

  @PatchMapping("/{id}/contabilizar")
  @Operation(summary = "Generar asientos contables y marcar como CONTABILIZADA")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> contabilizar(@PathVariable Long id) {
    return createSuccessResponse(liquidacionDiariaService.marcarContabilizada(id));
  }
}
