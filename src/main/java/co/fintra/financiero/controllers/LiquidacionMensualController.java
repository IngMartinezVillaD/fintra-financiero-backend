package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.request.liquidacion.IniciarLiquidacionRequestDto;
import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.services.interfaces.ILiquidacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/liquidaciones-mensuales")
@RequiredArgsConstructor
@Tag(name = "Liquidación Mensual", description = "Cierre mensual de intereses y plantillas ERP")
public class LiquidacionMensualController extends BaseController {

  private final ILiquidacionService liquidacionService;

  @PostMapping
  @Operation(summary = "Iniciar liquidación mensual (crea en estado BORRADOR)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> iniciar(@Valid @RequestBody IniciarLiquidacionRequestDto req) {
    return createCustomResponse(liquidacionService.iniciar(req), "Liquidación iniciada", HttpStatus.CREATED);
  }

  @PostMapping("/{id}/calcular")
  @Operation(summary = "Ejecutar motor de cálculo → pasa a PENDIENTE_APROBACION")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> calcular(@PathVariable Long id) {
    return createSuccessResponse(liquidacionService.calcular(id));
  }

  @GetMapping
  @Operation(summary = "Listar liquidaciones mensuales")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar() {
    return createSuccessResponse(liquidacionService.listar());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalle completo de la liquidación con línea por operación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(liquidacionService.obtener(id));
  }

  @PatchMapping("/{id}/aprobar")
  @Operation(summary = "Aprobar liquidación (PENDIENTE_APROBACION → APROBADA)")
  @PreAuthorize("hasAnyAuthority('ADMIN','APROBADOR')")
  public ResponseEntity<ApiResponseDto> aprobar(@PathVariable Long id) {
    return createSuccessResponse(liquidacionService.aprobar(id));
  }

  @PatchMapping("/{id}/revertir")
  @Operation(summary = "Revertir liquidación a BORRADOR (solo en BORRADOR o PENDIENTE_APROBACION)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> revertir(@PathVariable Long id) {
    return createSuccessResponse(liquidacionService.revertir(id));
  }

  @PatchMapping("/{id}/contabilizada")
  @Operation(summary = "Marcar como contabilizada tras cargar plantillas en ERP")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> contabilizada(@PathVariable Long id) {
    return createSuccessResponse(liquidacionService.marcarContabilizada(id));
  }

  @GetMapping("/{id}/plantillas/{empresaId}/descargar")
  @Operation(summary = "Descargar plantilla ERP por empresa (solo APROBADA o CONTABILIZADA)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> descargarPlantilla(@PathVariable Long id, @PathVariable Long empresaId) {
    byte[] contenido = liquidacionService.descargarPlantilla(id, empresaId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"plantilla-liq-" + id + "-empresa-" + empresaId + ".csv\"")
        .contentType(MediaType.TEXT_PLAIN)
        .body(contenido);
  }
}
