package co.pluto.services.impl;

import co.pluto.dto.request.interfaz.CrearInterfazContableRequestDto;
import co.pluto.dto.response.interfaz.InterfazContableResponseDto;
import co.pluto.dto.response.interfaz.TipoMovimientoContableDto;
import co.pluto.models.entity.EmpresaEntity;
import co.pluto.models.entity.InterfazContableEntity;
import co.pluto.models.entity.InterfazContableLineaEntity;
import co.pluto.models.entity.PucEntity;
import co.pluto.models.entity.TipoMovimientoContableEntity;
import co.pluto.models.repositories.IEmpresaRepository;
import co.pluto.models.repositories.IInterfazContableLineaRepository;
import co.pluto.models.repositories.IInterfazContableRepository;
import co.pluto.models.repositories.IPucRepository;
import co.pluto.models.repositories.ITipoMovimientoContableRepository;
import co.pluto.services.interfaces.IInterfazContableService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterfazContableServiceImpl implements IInterfazContableService {

  private final IInterfazContableRepository       interfazRepo;
  private final IInterfazContableLineaRepository  lineaRepo;
  private final IPucRepository                    pucRepo;
  private final IEmpresaRepository                empresaRepo;
  private final ITipoMovimientoContableRepository tipoMovRepo;

  @Override
  @Transactional(readOnly = true)
  public List<InterfazContableResponseDto> listar() {
    return interfazRepo.findAllByDeletedAtIsNullOrderByEmpresaIdAscTipoMovimientoIdAsc()
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<InterfazContableResponseDto> listarPorEmpresa(Long empresaId) {
    return interfazRepo.findAllByEmpresaIdAndDeletedAtIsNull(empresaId)
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public InterfazContableResponseDto obtener(Long id) {
    return interfazRepo.findByIdAndDeletedAtIsNull(id)
        .map(this::toDto)
        .orElseThrow(() -> new CustomException("Interfaz contable no encontrada", HttpStatus.NOT_FOUND));
  }

  @Override
  @Transactional
  public InterfazContableResponseDto crear(CrearInterfazContableRequestDto req) {
    // Validar empresa
    EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(req.getEmpresaId())
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));

    // Validar tipo de movimiento
    TipoMovimientoContableEntity tipo = tipoMovRepo.findById(req.getTipoMovimientoId())
        .filter(t -> Boolean.TRUE.equals(t.getActivo()))
        .orElseThrow(() -> new CustomException("Tipo de movimiento contable no encontrado", HttpStatus.NOT_FOUND));

    // Validar unicidad empresa + tipo
    if (interfazRepo.existsByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(
            req.getEmpresaId(), req.getTipoMovimientoId())) {
      throw new BusinessException(
          "Ya existe una interfaz contable para la empresa " +
          empresa.getRazonSocial() + " y el tipo de movimiento " + tipo.getNombre());
    }

    InterfazContableEntity entity = InterfazContableEntity.builder()
        .empresaId(req.getEmpresaId())
        .tipoMovimientoId(req.getTipoMovimientoId())
        .nombre(req.getNombre())
        .descripcion(req.getDescripcion())
        .activa(true)
        .build();

    entity = interfazRepo.save(entity);

    guardarLineas(entity.getId(), req.getLineas());

    return toDto(entity);
  }

  @Override
  @Transactional
  public InterfazContableResponseDto actualizar(Long id, CrearInterfazContableRequestDto req) {
    InterfazContableEntity entity = interfazRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Interfaz contable no encontrada", HttpStatus.NOT_FOUND));

    // Validar empresa
    EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(req.getEmpresaId())
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));

    // Validar tipo de movimiento
    TipoMovimientoContableEntity tipo = tipoMovRepo.findById(req.getTipoMovimientoId())
        .filter(t -> Boolean.TRUE.equals(t.getActivo()))
        .orElseThrow(() -> new CustomException("Tipo de movimiento contable no encontrado", HttpStatus.NOT_FOUND));

    // Validar unicidad si cambia empresa o tipo (ignorando el registro actual)
    boolean cambiaEmpresaOTipo = !entity.getEmpresaId().equals(req.getEmpresaId()) ||
                                 !entity.getTipoMovimientoId().equals(req.getTipoMovimientoId());

    if (cambiaEmpresaOTipo &&
        interfazRepo.existsByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(
            req.getEmpresaId(), req.getTipoMovimientoId())) {
      throw new BusinessException(
          "Ya existe una interfaz contable para la empresa " +
          empresa.getRazonSocial() + " y el tipo de movimiento " + tipo.getNombre());
    }

    entity.setEmpresaId(req.getEmpresaId());
    entity.setTipoMovimientoId(req.getTipoMovimientoId());
    entity.setNombre(req.getNombre());
    entity.setDescripcion(req.getDescripcion());

    entity = interfazRepo.save(entity);

    // Borrar líneas existentes y recrear
    lineaRepo.deleteAllByInterfazId(entity.getId());
    guardarLineas(entity.getId(), req.getLineas());

    return toDto(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TipoMovimientoContableDto> listarTiposMovimiento() {
    return tipoMovRepo.findAllByActivoIsTrue()
        .stream()
        .map(t -> TipoMovimientoContableDto.builder()
            .id(t.getId())
            .codigo(t.getCodigo())
            .nombre(t.getNombre())
            .descripcion(t.getDescripcion())
            .build())
        .toList();
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private void guardarLineas(Long interfazId, List<CrearInterfazContableRequestDto.LineaDto> lineas) {
    for (CrearInterfazContableRequestDto.LineaDto l : lineas) {
      // Validar cuenta PUC
      if (pucRepo.findByIdAndDeletedAtIsNull(l.getCuentaPucId()).isEmpty()) {
        throw new CustomException("Cuenta PUC con id " + l.getCuentaPucId() + " no encontrada", HttpStatus.NOT_FOUND);
      }

      InterfazContableLineaEntity linea = InterfazContableLineaEntity.builder()
          .interfazId(interfazId)
          .orden(l.getOrden())
          .cuentaPucId(l.getCuentaPucId())
          .naturaleza(l.getNaturaleza())
          .descripcionGlosa(l.getDescripcionGlosa())
          .build();

      lineaRepo.save(linea);
    }
  }

  private InterfazContableResponseDto toDto(InterfazContableEntity e) {
    // Resolver empresa
    EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(e.getEmpresaId()).orElse(null);
    // Resolver tipo de movimiento
    TipoMovimientoContableEntity tipo = tipoMovRepo.findById(e.getTipoMovimientoId()).orElse(null);

    // Resolver líneas
    List<InterfazContableLineaEntity> lineasEntidad =
        lineaRepo.findAllByInterfazIdOrderByOrdenAsc(e.getId());

    // Pre-cargar cuentas PUC para evitar N+1
    List<Long> pucIds = lineasEntidad.stream()
        .map(InterfazContableLineaEntity::getCuentaPucId)
        .distinct().toList();

    Map<Long, PucEntity> pucMap = pucRepo.findAllById(pucIds).stream()
        .collect(Collectors.toMap(PucEntity::getId, Function.identity()));

    List<InterfazContableResponseDto.LineaDto> lineasDto = lineasEntidad.stream()
        .map(l -> {
          PucEntity puc = pucMap.get(l.getCuentaPucId());
          return InterfazContableResponseDto.LineaDto.builder()
              .id(l.getId())
              .orden(l.getOrden())
              .cuentaPucId(l.getCuentaPucId())
              .cuentaPucCodigo(puc != null ? puc.getCodigo() : null)
              .cuentaPucNombre(puc != null ? puc.getNombre() : null)
              .naturaleza(l.getNaturaleza())
              .descripcionGlosa(l.getDescripcionGlosa())
              .build();
        }).toList();

    return InterfazContableResponseDto.builder()
        .id(e.getId())
        .empresaId(e.getEmpresaId())
        .empresaNombre(empresa != null ? empresa.getRazonSocial() : null)
        .empresaNit(empresa != null ? empresa.getNit() : null)
        .tipoMovimientoId(e.getTipoMovimientoId())
        .tipoMovimientoCodigo(tipo != null ? tipo.getCodigo() : null)
        .tipoMovimientoNombre(tipo != null ? tipo.getNombre() : null)
        .nombre(e.getNombre())
        .descripcion(e.getDescripcion())
        .activa(Boolean.TRUE.equals(e.getActiva()))
        .createdAt(e.getCreatedAt())
        .lineas(lineasDto)
        .build();
  }
}
