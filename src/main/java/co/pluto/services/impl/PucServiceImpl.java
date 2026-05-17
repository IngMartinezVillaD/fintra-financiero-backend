package co.pluto.services.impl;

import co.pluto.dto.request.puc.ActualizarPucRequestDto;
import co.pluto.dto.request.puc.CrearPucRequestDto;
import co.pluto.dto.response.puc.PucResponseDto;
import co.pluto.models.entity.PucEntity;
import co.pluto.models.repositories.IPucRepository;
import co.pluto.services.interfaces.IPucService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PucServiceImpl implements IPucService {

  private static final Set<Integer> LONGITUDES_VALIDAS = Set.of(1, 2, 4, 6, 8);

  private final IPucRepository pucRepo;

  @Override
  @Transactional(readOnly = true)
  public List<PucResponseDto> listar() {
    return pucRepo.findAllByDeletedAtIsNullOrderByCodigoAsc()
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PucResponseDto> buscar(String q) {
    if (q == null || q.isBlank()) {
      return listar();
    }
    return pucRepo.findAllByCodigoContainingOrNombreContainingIgnoreCaseAndDeletedAtIsNull(q, q)
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PucResponseDto obtener(Long id) {
    return pucRepo.findByIdAndDeletedAtIsNull(id)
        .map(this::toDto)
        .orElseThrow(() -> new CustomException("Cuenta PUC no encontrada", HttpStatus.NOT_FOUND));
  }

  @Override
  @Transactional
  public PucResponseDto crear(CrearPucRequestDto req) {
    String codigo = req.getCodigo().trim();
    int largo = codigo.length();

    if (!LONGITUDES_VALIDAS.contains(largo)) {
      throw new BusinessException("El código debe tener 1, 2, 4, 6 u 8 dígitos");
    }

    if (pucRepo.findByCodigoAndDeletedAtIsNull(codigo).isPresent()) {
      throw new BusinessException("Ya existe una cuenta con el código " + codigo);
    }

    PucEntity entity = PucEntity.builder()
        .codigo(codigo)
        .nombre(req.getNombre().trim())
        .tipo(req.getTipo())
        .naturaleza(req.getNaturaleza())
        .nivel(calcularNivel(largo))
        .activa(true)
        .aplicaCentroCosto(Boolean.TRUE.equals(req.getAplicaCentroCosto()))
        .build();

    return toDto(pucRepo.save(entity));
  }

  @Override
  @Transactional
  public PucResponseDto actualizar(Long id, ActualizarPucRequestDto req) {
    PucEntity entity = pucRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Cuenta PUC no encontrada", HttpStatus.NOT_FOUND));

    entity.setNombre(req.getNombre().trim());
    if (req.getTipo() != null && !req.getTipo().isBlank()) {
      entity.setTipo(req.getTipo());
    }
    if (req.getNaturaleza() != null && !req.getNaturaleza().isBlank()) {
      entity.setNaturaleza(req.getNaturaleza());
    }
    if (req.getAplicaCentroCosto() != null) {
      entity.setAplicaCentroCosto(req.getAplicaCentroCosto());
    }

    return toDto(pucRepo.save(entity));
  }

  @Override
  @Transactional
  public void activar(Long id) {
    PucEntity entity = pucRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Cuenta PUC no encontrada", HttpStatus.NOT_FOUND));
    entity.setActiva(true);
    pucRepo.save(entity);
  }

  @Override
  @Transactional
  public void inactivar(Long id) {
    PucEntity entity = pucRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Cuenta PUC no encontrada", HttpStatus.NOT_FOUND));

    // Buscar hijas activas (cuentas cuyo código empieza con este código pero no es el mismo)
    List<PucEntity> hijasActivas = pucRepo
        .findAllByCodigoStartingWithAndActivaIsTrueAndDeletedAtIsNull(entity.getCodigo())
        .stream()
        .filter(h -> !h.getId().equals(entity.getId()))
        .toList();

    if (!hijasActivas.isEmpty()) {
      throw new BusinessException(
          "No se puede inactivar la cuenta " + entity.getCodigo() +
          " - " + entity.getNombre() +
          " porque tiene subcuentas activas");
    }

    entity.setActiva(false);
    pucRepo.save(entity);
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private Short calcularNivel(int largo) {
    return switch (largo) {
      case 1 -> (short) 1;
      case 2 -> (short) 2;
      case 4 -> (short) 4;
      case 6 -> (short) 6;
      case 8 -> (short) 8;
      default -> throw new BusinessException("Longitud de código no válida: " + largo);
    };
  }

  private String nivelNombre(Short nivel) {
    if (nivel == null) return "";
    return switch (nivel) {
      case 1 -> "Clase";
      case 2 -> "Grupo";
      case 4 -> "Cuenta";
      case 6 -> "Subcuenta";
      case 8 -> "Auxiliar";
      default -> "Nivel " + nivel;
    };
  }

  private PucResponseDto toDto(PucEntity e) {
    return PucResponseDto.builder()
        .id(e.getId())
        .codigo(e.getCodigo())
        .nombre(e.getNombre())
        .tipo(e.getTipo())
        .naturaleza(e.getNaturaleza())
        .nivel(e.getNivel())
        .nivelNombre(nivelNombre(e.getNivel()))
        .activa(Boolean.TRUE.equals(e.getActiva()))
        .aplicaCentroCosto(Boolean.TRUE.equals(e.getAplicaCentroCosto()))
        .createdAt(e.getCreatedAt())
        .build();
  }
}
