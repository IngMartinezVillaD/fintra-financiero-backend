package co.pluto.services.impl;

import co.pluto.dto.request.saldos.CrearSaldoInicialRequestDto;
import co.pluto.dto.response.saldos.SaldoInicialResponseDto;
import co.pluto.models.entity.EmpresaEntity;
import co.pluto.models.entity.SaldoInicialEntity;
import co.pluto.models.repositories.IEmpresaRepository;
import co.pluto.models.repositories.ISaldoInicialRepository;
import co.pluto.services.interfaces.ISaldoInicialService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaldoInicialServiceImpl implements ISaldoInicialService {

  private final ISaldoInicialRepository saldoRepo;
  private final IEmpresaRepository      empresaRepo;

  @Override
  @Transactional
  public SaldoInicialResponseDto crear(CrearSaldoInicialRequestDto req, String username) {
    if (req.getEmpresaPrestamistaId().equals(req.getEmpresaPrestatariaId()))
      throw new BusinessException("La empresa prestamista y prestataria deben ser diferentes");

    EmpresaEntity prestamista = empresaRepo.findByIdAndDeletedAtIsNull(req.getEmpresaPrestamistaId())
        .orElseThrow(() -> new CustomException("Empresa prestamista no encontrada", HttpStatus.NOT_FOUND));

    EmpresaEntity prestataria = empresaRepo.findByIdAndDeletedAtIsNull(req.getEmpresaPrestatariaId())
        .orElseThrow(() -> new CustomException("Empresa prestataria no encontrada", HttpStatus.NOT_FOUND));

    BigDecimal intereses = req.getInteresesAcumulados() != null
        ? req.getInteresesAcumulados()
        : BigDecimal.ZERO;

    SaldoInicialEntity saldo = SaldoInicialEntity.builder()
        .codigo(generarCodigo())
        .empresaPrestamista(prestamista)
        .empresaPrestataria(prestataria)
        .tipoTasa(req.getTipoTasa())
        .tasaPorcentajeMensual(req.getTasaPorcentajeMensual())
        .saldoCapital(req.getSaldoCapital())
        .interesesAcumulados(intereses)
        .fechaCorte(req.getFechaCorte())
        .estado("ACTIVO")
        .observaciones(req.getObservaciones())
        .build();

    return toDto(saldoRepo.save(saldo));
  }

  @Override
  @Transactional(readOnly = true)
  public List<SaldoInicialResponseDto> listar() {
    return saldoRepo.findAllByDeletedAtIsNullOrderByIdDesc()
        .stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SaldoInicialResponseDto getById(Long id) {
    return saldoRepo.findByIdAndDeletedAtIsNull(id)
        .map(this::toDto)
        .orElseThrow(() -> new CustomException("Saldo inicial no encontrado", HttpStatus.NOT_FOUND));
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private String generarCodigo() {
    int maxNum = saldoRepo.findAllCodigosSld().stream()
        .map(c -> c.replaceAll("^SLD-(\\d+)$", "$1"))
        .filter(s -> s.matches("\\d+"))
        .mapToInt(Integer::parseInt)
        .max()
        .orElse(0);
    String codigo = String.format("SLD-%02d", maxNum + 1);
    while (saldoRepo.existsByCodigo(codigo)) {
      codigo = String.format("SLD-%02d", ++maxNum + 1);
    }
    return codigo;
  }

  private SaldoInicialResponseDto toDto(SaldoInicialEntity e) {
    return SaldoInicialResponseDto.builder()
        .id(e.getId())
        .codigo(e.getCodigo())
        .empresaPrestamistaId(e.getEmpresaPrestamista().getId())
        .empresaPrestamistaNombre(e.getEmpresaPrestamista().getRazonSocial())
        .empresaPrestamistaNit(e.getEmpresaPrestamista().getNit())
        .empresaPrestatariaId(e.getEmpresaPrestataria().getId())
        .empresaPrestatariaNombre(e.getEmpresaPrestataria().getRazonSocial())
        .empresaPrestatariaNit(e.getEmpresaPrestataria().getNit())
        .tipoTasa(e.getTipoTasa())
        .tasaPorcentajeMensual(e.getTasaPorcentajeMensual())
        .saldoCapital(e.getSaldoCapital())
        .interesesAcumulados(e.getInteresesAcumulados())
        .fechaCorte(e.getFechaCorte())
        .estado(e.getEstado())
        .observaciones(e.getObservaciones())
        .createdAt(e.getCreatedAt())
        .build();
  }
}
