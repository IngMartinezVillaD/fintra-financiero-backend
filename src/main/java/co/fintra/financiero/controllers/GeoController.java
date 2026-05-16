package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.request.geo.*;
import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/geo")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Geografía", description = "Catálogo de países, departamentos y ciudades — DIVIPOLA/DANE")
public class GeoController extends BaseController {

  private final IPaisRepository          paisRepo;
  private final IDepartamentoRepository  departamentoRepo;
  private final ICiudadRepository        ciudadRepo;

  // ─────────────────────────────────────────────────────── PAÍSES

  @GetMapping("/paises")
  @Operation(summary = "Listar países activos (para selects)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponseDto> listarPaises() {
    return createSuccessResponse(paisRepo.findAllByActivoTrueOrderByNombreAsc().stream()
        .map(this::toPaisMap).toList());
  }

  @GetMapping("/paises/admin")
  @Operation(summary = "Listar todos los países (admin)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> listarPaisesAdmin() {
    return createSuccessResponse(paisRepo.findAllByOrderByNombreAsc().stream()
        .map(this::toPaisMap).toList());
  }

  @PostMapping("/paises")
  @Operation(summary = "Crear país")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> crearPais(@Valid @RequestBody PaisRequestDto req) {
    if (paisRepo.existsByCodigoIso2(req.getCodigoIso2()))
      throw new BusinessException("Ya existe un país con código ISO-2 '" + req.getCodigoIso2() + "'");
    if (paisRepo.existsByCodigoIso3(req.getCodigoIso3()))
      throw new BusinessException("Ya existe un país con código ISO-3 '" + req.getCodigoIso3() + "'");
    PaisEntity p = PaisEntity.builder()
        .codigoIso2(req.getCodigoIso2().toUpperCase())
        .codigoIso3(req.getCodigoIso3().toUpperCase())
        .nombre(req.getNombre())
        .activo(true)
        .build();
    return createCustomResponse(toPaisMap(paisRepo.save(p)), "País creado", HttpStatus.CREATED);
  }

  @PutMapping("/paises/{id}")
  @Operation(summary = "Actualizar país")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> actualizarPais(@PathVariable Integer id,
                                                        @Valid @RequestBody PaisRequestDto req) {
    PaisEntity p = paisRepo.findById(id)
        .orElseThrow(() -> new CustomException("País no encontrado", HttpStatus.NOT_FOUND));
    if (paisRepo.existsByCodigoIso2AndIdNot(req.getCodigoIso2(), id))
      throw new BusinessException("Ya existe otro país con código ISO-2 '" + req.getCodigoIso2() + "'");
    if (paisRepo.existsByCodigoIso3AndIdNot(req.getCodigoIso3(), id))
      throw new BusinessException("Ya existe otro país con código ISO-3 '" + req.getCodigoIso3() + "'");
    p.setCodigoIso2(req.getCodigoIso2().toUpperCase());
    p.setCodigoIso3(req.getCodigoIso3().toUpperCase());
    p.setNombre(req.getNombre());
    return createSuccessResponse(toPaisMap(paisRepo.save(p)));
  }

  @PatchMapping("/paises/{id}/estado")
  @Operation(summary = "Activar / inactivar país")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> estadoPais(@PathVariable Integer id,
                                                    @RequestParam boolean activo) {
    PaisEntity p = paisRepo.findById(id)
        .orElseThrow(() -> new CustomException("País no encontrado", HttpStatus.NOT_FOUND));
    p.setActivo(activo);
    return createSuccessResponse(toPaisMap(paisRepo.save(p)));
  }

  // ─────────────────────────────────────────────────── DEPARTAMENTOS

  @GetMapping("/departamentos")
  @Operation(summary = "Listar departamentos activos por país (para selects)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponseDto> listarDepartamentos(
      @RequestParam(defaultValue = "CO") String paisCodigo) {
    return createSuccessResponse(
        departamentoRepo.findAllByPaisCodigoIso2AndActivoTrueOrderByNombreAsc(paisCodigo.toUpperCase())
            .stream().map(this::toDeptMap).toList());
  }

  @GetMapping("/departamentos/admin")
  @Operation(summary = "Listar todos los departamentos de un país (admin)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> listarDepartamentosAdmin(
      @RequestParam(defaultValue = "CO") String paisCodigo) {
    return createSuccessResponse(
        departamentoRepo.findAllByPaisCodigoIso2OrderByNombreAsc(paisCodigo.toUpperCase())
            .stream().map(this::toDeptMap).toList());
  }

  @PostMapping("/departamentos")
  @Operation(summary = "Crear departamento")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> crearDepartamento(@Valid @RequestBody DepartamentoRequestDto req) {
    PaisEntity pais = paisRepo.findById(req.getPaisId())
        .orElseThrow(() -> new BusinessException("País no encontrado"));
    if (departamentoRepo.existsByCodigoDaneAndPaisId(req.getCodigoDane(), req.getPaisId()))
      throw new BusinessException("Ya existe un departamento con código DANE '" + req.getCodigoDane() + "' en ese país");
    DepartamentoEntity d = DepartamentoEntity.builder()
        .codigoDane(req.getCodigoDane())
        .nombre(req.getNombre())
        .pais(pais)
        .activo(true)
        .build();
    return createCustomResponse(toDeptMap(departamentoRepo.save(d)), "Departamento creado", HttpStatus.CREATED);
  }

  @PutMapping("/departamentos/{id}")
  @Operation(summary = "Actualizar departamento")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> actualizarDepartamento(@PathVariable Integer id,
                                                                @Valid @RequestBody DepartamentoRequestDto req) {
    DepartamentoEntity d = departamentoRepo.findById(id)
        .orElseThrow(() -> new CustomException("Departamento no encontrado", HttpStatus.NOT_FOUND));
    PaisEntity pais = paisRepo.findById(req.getPaisId())
        .orElseThrow(() -> new BusinessException("País no encontrado"));
    if (departamentoRepo.existsByCodigoDaneAndPaisIdAndIdNot(req.getCodigoDane(), req.getPaisId(), id))
      throw new BusinessException("Ya existe otro departamento con ese código DANE en ese país");
    d.setCodigoDane(req.getCodigoDane());
    d.setNombre(req.getNombre());
    d.setPais(pais);
    return createSuccessResponse(toDeptMap(departamentoRepo.save(d)));
  }

  @PatchMapping("/departamentos/{id}/estado")
  @Operation(summary = "Activar / inactivar departamento")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> estadoDepartamento(@PathVariable Integer id,
                                                            @RequestParam boolean activo) {
    DepartamentoEntity d = departamentoRepo.findById(id)
        .orElseThrow(() -> new CustomException("Departamento no encontrado", HttpStatus.NOT_FOUND));
    d.setActivo(activo);
    return createSuccessResponse(toDeptMap(departamentoRepo.save(d)));
  }

  // ────────────────────────────────────────────────────── CIUDADES

  @GetMapping("/ciudades")
  @Operation(summary = "Listar ciudades activas por departamento (para selects)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponseDto> listarCiudades(@RequestParam String departamentoCodigo) {
    return createSuccessResponse(
        ciudadRepo.findAllByDepartamentoCodigoDaneAndActivoTrueOrderByNombreAsc(departamentoCodigo)
            .stream().map(this::toCiudadMap).toList());
  }

  @GetMapping("/ciudades/admin")
  @Operation(summary = "Listar todas las ciudades de un departamento (admin)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> listarCiudadesAdmin(@RequestParam String departamentoCodigo) {
    return createSuccessResponse(
        ciudadRepo.findAllByDepartamentoCodigoDaneOrderByNombreAsc(departamentoCodigo)
            .stream().map(this::toCiudadMap).toList());
  }

  @PostMapping("/ciudades")
  @Operation(summary = "Crear ciudad")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> crearCiudad(@Valid @RequestBody CiudadRequestDto req) {
    DepartamentoEntity depto = departamentoRepo.findById(req.getDepartamentoId())
        .orElseThrow(() -> new BusinessException("Departamento no encontrado"));
    if (ciudadRepo.existsByCodigoDane(req.getCodigoDane()))
      throw new BusinessException("Ya existe una ciudad con código DANE '" + req.getCodigoDane() + "'");
    CiudadEntity c = CiudadEntity.builder()
        .codigoDane(req.getCodigoDane())
        .nombre(req.getNombre())
        .codigoPostal(req.getCodigoPostal())
        .departamento(depto)
        .activo(true)
        .build();
    return createCustomResponse(toCiudadMap(ciudadRepo.save(c)), "Ciudad creada", HttpStatus.CREATED);
  }

  @PutMapping("/ciudades/{id}")
  @Operation(summary = "Actualizar ciudad")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> actualizarCiudad(@PathVariable Integer id,
                                                          @Valid @RequestBody CiudadRequestDto req) {
    CiudadEntity c = ciudadRepo.findById(id)
        .orElseThrow(() -> new CustomException("Ciudad no encontrada", HttpStatus.NOT_FOUND));
    DepartamentoEntity depto = departamentoRepo.findById(req.getDepartamentoId())
        .orElseThrow(() -> new BusinessException("Departamento no encontrado"));
    if (ciudadRepo.existsByCodigoDaneAndIdNot(req.getCodigoDane(), id))
      throw new BusinessException("Ya existe otra ciudad con código DANE '" + req.getCodigoDane() + "'");
    c.setCodigoDane(req.getCodigoDane());
    c.setNombre(req.getNombre());
    c.setCodigoPostal(req.getCodigoPostal());
    c.setDepartamento(depto);
    return createSuccessResponse(toCiudadMap(ciudadRepo.save(c)));
  }

  @PatchMapping("/ciudades/{id}/estado")
  @Operation(summary = "Activar / inactivar ciudad")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponseDto> estadoCiudad(@PathVariable Integer id,
                                                      @RequestParam boolean activo) {
    CiudadEntity c = ciudadRepo.findById(id)
        .orElseThrow(() -> new CustomException("Ciudad no encontrada", HttpStatus.NOT_FOUND));
    c.setActivo(activo);
    return createSuccessResponse(toCiudadMap(ciudadRepo.save(c)));
  }

  // ────────────────────────────────────────────────────── MAPPERS

  private Map<String, Object> toPaisMap(PaisEntity p) {
    return Map.of("id", p.getId(), "codigoIso2", p.getCodigoIso2(),
        "codigoIso3", p.getCodigoIso3(), "nombre", p.getNombre(), "activo", p.getActivo());
  }

  private Map<String, Object> toDeptMap(DepartamentoEntity d) {
    return Map.of("id", d.getId(), "codigoDane", d.getCodigoDane(),
        "nombre", d.getNombre(), "paisId", d.getPais().getId(),
        "paisNombre", d.getPais().getNombre(), "activo", d.getActivo());
  }

  private Map<String, Object> toCiudadMap(CiudadEntity c) {
    return Map.ofEntries(
        Map.entry("id",                c.getId()),
        Map.entry("codigoDane",        c.getCodigoDane()),
        Map.entry("nombre",            c.getNombre()),
        Map.entry("codigoPostal",      c.getCodigoPostal() != null ? c.getCodigoPostal() : ""),
        Map.entry("departamentoId",    c.getDepartamento().getId()),
        Map.entry("departamentoNombre",c.getDepartamento().getNombre()),
        Map.entry("activo",            c.getActivo())
    );
  }
}
