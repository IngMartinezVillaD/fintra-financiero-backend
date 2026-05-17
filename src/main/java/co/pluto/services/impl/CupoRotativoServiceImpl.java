package co.pluto.services.impl;

import co.pluto.dto.request.cupos.ActualizarCupoRotativoRequestDto;
import co.pluto.dto.request.cupos.CrearCupoRotativoRequestDto;
import co.pluto.dto.response.cupos.CupoRotativoResponseDto;
import co.pluto.models.entity.CupoRotativoEntity;
import co.pluto.models.entity.EmpresaEntity;
import co.pluto.models.repositories.IEmpresaRepository;
import co.pluto.models.repositories.ICupoRotativoRepository;
import co.pluto.services.interfaces.ICupoRotativoService;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CupoRotativoServiceImpl implements ICupoRotativoService {

  private final ICupoRotativoRepository cupoRepo;
  private final IEmpresaRepository      empresaRepo;

  @Override
  @Transactional
  public CupoRotativoResponseDto crear(CrearCupoRotativoRequestDto req, String username) {
    EmpresaEntity empresa = empresaRepo.findByIdAndDeletedAtIsNull(req.getEmpresaId())
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));

    CupoRotativoEntity cupo = CupoRotativoEntity.builder()
        .codigo(generarCodigo())
        .empresa(empresa)
        .tipoTasa(req.getTipoTasa())
        .tasaPorcentajeMensual(req.getTasaPorcentajeMensual())
        .valorCupo(req.getValorCupo())
        .saldoDisponible(req.getValorCupo())
        .estado("ACTIVO")
        .observaciones(req.getObservaciones())
        .build();

    return toDto(cupoRepo.save(cupo));
  }

  @Override
  @Transactional
  public CupoRotativoResponseDto actualizar(Long id, ActualizarCupoRotativoRequestDto req) {
    CupoRotativoEntity cupo = cupoRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Cupo rotativo no encontrado", HttpStatus.NOT_FOUND));

    // Si aumenta el cupo, incrementar el saldo disponible en la misma diferencia
    java.math.BigDecimal diferencia = req.getValorCupo().subtract(cupo.getValorCupo());
    java.math.BigDecimal nuevoSaldo = cupo.getSaldoDisponible().add(diferencia);
    if (nuevoSaldo.compareTo(java.math.BigDecimal.ZERO) < 0) {
      throw new CustomException(
          "El nuevo valor del cupo no puede ser menor al monto ya utilizado",
          HttpStatus.BAD_REQUEST);
    }

    cupo.setValorCupo(req.getValorCupo());
    cupo.setSaldoDisponible(nuevoSaldo);
    cupo.setEstado(req.getEstado());
    cupo.setObservaciones(req.getObservaciones());

    return toDto(cupoRepo.save(cupo));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CupoRotativoResponseDto> listar() {
    return cupoRepo.findAllByDeletedAtIsNullOrderByIdDesc()
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public CupoRotativoResponseDto getById(Long id) {
    return cupoRepo.findByIdAndDeletedAtIsNull(id)
        .map(this::toDto)
        .orElseThrow(() -> new CustomException("Cupo rotativo no encontrado", HttpStatus.NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CupoRotativoResponseDto> listarActivosPorEmpresa(Long empresaId) {
    return cupoRepo.findAllByEmpresaIdAndEstadoAndDeletedAtIsNull(empresaId, "ACTIVO")
        .stream().map(this::toDto).toList();
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private String generarCodigo() {
    int maxNum = cupoRepo.findAllCodigosCup().stream()
        .map(c -> c.replaceAll("^CUP-(\\d+)$", "$1"))
        .filter(s -> s.matches("\\d+"))
        .mapToInt(Integer::parseInt)
        .max()
        .orElse(0);
    String codigo = String.format("CUP-%02d", maxNum + 1);
    while (cupoRepo.existsByCodigo(codigo)) {
      codigo = String.format("CUP-%02d", ++maxNum + 1);
    }
    return codigo;
  }

  private CupoRotativoResponseDto toDto(CupoRotativoEntity e) {
    return CupoRotativoResponseDto.builder()
        .id(e.getId())
        .codigo(e.getCodigo())
        .empresaId(e.getEmpresa().getId())
        .empresaNombre(e.getEmpresa().getRazonSocial())
        .empresaNit(e.getEmpresa().getNit())
        .tipoTasa(e.getTipoTasa())
        .tasaPorcentajeMensual(e.getTasaPorcentajeMensual())
        .valorCupo(e.getValorCupo())
        .saldoDisponible(e.getSaldoDisponible())
        .estado(e.getEstado())
        .observaciones(e.getObservaciones())
        .createdAt(e.getCreatedAt())
        .build();
  }
}
