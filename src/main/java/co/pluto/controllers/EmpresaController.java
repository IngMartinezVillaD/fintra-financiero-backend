package co.pluto.controllers;

import co.pluto.dto.request.empresa.*;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.models.repositories.IBancoRepository;
import co.pluto.services.interfaces.IEmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
@Tag(name = "Maestro de Empresas", description = "CRUD de empresas, cuentas bancarias y tasas especiales")
public class EmpresaController extends BaseController {

  private final IEmpresaService empresaService;
  private final IBancoRepository bancoRepo;

  // ──────────────────────────────────────────────── BANCOS

  @GetMapping("/bancos")
  @Operation(summary = "Listar bancos activos")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarBancos() {
    var bancos = bancoRepo.findAllByActivoTrueOrderByNombreAsc().stream()
        .map(b -> Map.of("codigo", b.getCodigo(), "nombre", b.getNombre()))
        .toList();
    return createSuccessResponse(bancos);
  }

  @GetMapping("/cuentas-bancarias")
  @Operation(summary = "Consulta de todas las cuentas bancarias (todas las empresas)")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listarTodasCuentas(
      @RequestParam(required = false) String bancoCodigo,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) Boolean activa) {
    return createSuccessResponse(empresaService.listarTodasCuentasBancarias(bancoCodigo, tipo, activa));
  }

  // ──────────────────────────────────────────────── EMPRESAS

  @GetMapping
  @Operation(summary = "Listar empresas con filtros y paginación")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> listar(
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) String rolPermitido,
      @RequestParam(required = false) String busqueda,
      @RequestParam(defaultValue = "0")  int page,
      @RequestParam(defaultValue = "10") int size) {
    var pageable = PageRequest.of(page, size);
    return createSuccessResponse(empresaService.listar(estado, rolPermitido, busqueda, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener detalle de empresa")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD','CONSULTA')")
  public ResponseEntity<ApiResponseDto> obtener(@PathVariable Long id) {
    return createSuccessResponse(empresaService.obtener(id));
  }

  @PostMapping
  @Operation(summary = "Crear nueva empresa")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> crear(@Valid @RequestBody CrearEmpresaRequestDto request) {
    return createCustomResponse(empresaService.crear(request), "Empresa creada exitosamente", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar empresa")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody ActualizarEmpresaRequestDto request) {
    return createSuccessResponse(empresaService.actualizar(id, request));
  }

  @PatchMapping("/{id}/inactivar")
  @Operation(summary = "Inactivar empresa")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> inactivar(@PathVariable Long id) {
    empresaService.inactivar(id);
    return createSuccessResponse("Empresa inactivada");
  }

  // ──────────────────────────────────────────────── CUENTAS BANCARIAS

  @PostMapping("/{id}/cuentas-bancarias")
  @Operation(summary = "Agregar cuenta bancaria a empresa")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> agregarCuenta(
      @PathVariable Long id,
      @Valid @RequestBody CuentaBancariaRequestDto request) {
    return createCustomResponse(
        empresaService.agregarCuentaBancaria(id, request), "Cuenta bancaria agregada", HttpStatus.CREATED);
  }

  @PutMapping("/{id}/cuentas-bancarias/{cuentaId}")
  @Operation(summary = "Editar cuenta bancaria")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> editarCuenta(
      @PathVariable Long id,
      @PathVariable Long cuentaId,
      @Valid @RequestBody CuentaBancariaRequestDto request) {
    return createSuccessResponse(empresaService.editarCuentaBancaria(id, cuentaId, request));
  }

  @PatchMapping("/{id}/cuentas-bancarias/{cuentaId}/desactivar")
  @Operation(summary = "Desactivar cuenta bancaria")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> desactivarCuenta(
      @PathVariable Long id,
      @PathVariable Long cuentaId) {
    empresaService.desactivarCuentaBancaria(id, cuentaId);
    return createSuccessResponse("Cuenta bancaria desactivada");
  }

  // ──────────────────────────────────────────────── TASAS ESPECIALES

  @GetMapping("/{id}/tasas-especiales")
  @Operation(summary = "Listar historial de tasas especiales")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','APROBADOR','CONTABILIDAD')")
  public ResponseEntity<ApiResponseDto> listarTasas(@PathVariable Long id) {
    return createSuccessResponse(empresaService.listarTasasEspeciales(id));
  }

  @PostMapping("/{id}/tasas-especiales")
  @Operation(summary = "Solicitar nueva tasa especial")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA')")
  public ResponseEntity<ApiResponseDto> solicitarTasa(
      @PathVariable Long id,
      @Valid @RequestBody SolicitarTasaEspecialRequestDto request) {
    return createCustomResponse(
        empresaService.solicitarTasaEspecial(id, request), "Tasa especial solicitada", HttpStatus.CREATED);
  }

  @PatchMapping("/{id}/tasas-especiales/{tasaId}/aprobar")
  @Operation(summary = "Aprobar tasa especial")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> aprobarTasa(
      @PathVariable Long id,
      @PathVariable Long tasaId,
      @RequestBody(required = false) Map<String, String> body) {
    String obs = body != null ? body.get("observacion") : null;
    return createSuccessResponse(empresaService.aprobarTasaEspecial(id, tasaId, obs));
  }

  @PatchMapping("/{id}/tasas-especiales/{tasaId}/rechazar")
  @Operation(summary = "Rechazar tasa especial")
  @PreAuthorize("hasAnyAuthority('APROBADOR','ADMIN')")
  public ResponseEntity<ApiResponseDto> rechazarTasa(
      @PathVariable Long id,
      @PathVariable Long tasaId,
      @RequestBody(required = false) Map<String, String> body) {
    String obs = body != null ? body.get("observacion") : null;
    return createSuccessResponse(empresaService.rechazarTasaEspecial(id, tasaId, obs));
  }
}
