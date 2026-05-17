package co.pluto.services.impl;

import co.pluto.dto.request.controles.DecisionAnualGmfRequestDto;
import co.pluto.dto.response.controles.*;
import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.services.impl.controles.MotorInteresPresuntoService;
import co.pluto.services.interfaces.IControlesService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ControlesServiceImpl implements IControlesService {

  private final IGmfMovimientoRepository    gmfRepo;
  private final IInteresPresuntoRepository   presuntoRepo;
  private final IEmpresaRepository           empresaRepo;
  private final IOperacionRepository         operacionRepo;
  private final MotorInteresPresuntoService  motor;

  // ── GMF ─────────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<GmfEmpresaDto> consolidadoGmf(Short anio) {
    List<GmfMovimientoEntity> movimientos = gmfRepo.findAllByAnioOrderByMesAsc(anio);

    Map<Long, List<GmfMovimientoEntity>> porEmpresa = movimientos.stream()
        .collect(Collectors.groupingBy(GmfMovimientoEntity::getEmpresaId));

    return porEmpresa.entrySet().stream()
        .map(e -> buildGmfEmpresaDto(e.getKey(), anio, e.getValue()))
        .sorted(Comparator.comparing(GmfEmpresaDto::getRazonSocial))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public GmfEmpresaDto gmfPorEmpresa(Long empresaId, Short anio) {
    List<GmfMovimientoEntity> movs = gmfRepo.findAllByEmpresaIdAndAnioOrderByMesAsc(empresaId, anio);
    return buildGmfEmpresaDto(empresaId, anio, movs);
  }

  @Override
  public void registrarDecisionAnualGmf(DecisionAnualGmfRequestDto req) {
    if (!List.of("COBRAR", "ASUMIR").contains(req.getDecision()))
      throw new BusinessException("Decisión inválida. Use COBRAR o ASUMIR");

    List<GmfMovimientoEntity> movs = gmfRepo.findAllByEmpresaIdAndAnioOrderByMesAsc(req.getEmpresaId(), req.getAnio());
    if (movs.isEmpty())
      throw new BusinessException("No hay movimientos GMF para empresa " + req.getEmpresaId() + " año " + req.getAnio());

    boolean yaDecidido = movs.stream().anyMatch(m -> !"PENDIENTE".equals(m.getDecisionAnual()));
    if (yaDecidido)
      throw new BusinessException("Ya existe una decisión registrada para este año. No es editable.");

    movs.forEach(m -> m.setDecisionAnual(req.getDecision()));
    gmfRepo.saveAll(movs);
  }

  // ── Presunto ────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<PresuntoEmpresaDto> consolidadoPresunto(Short anio) {
    List<InteresPresuntoMovimientoEntity> todos = presuntoRepo
        .findAllByAnioAndMes(anio, (short) 1); // placeholder — need all months

    // Gather all distinct empresaIds with presunto in this year
    Set<Long> empresaIds = new HashSet<>();
    for (short mes = 1; mes <= 12; mes++) {
      presuntoRepo.findAllByAnioAndMes(anio, mes)
          .forEach(p -> empresaIds.add(p.getEmpresaId()));
    }

    return empresaIds.stream()
        .map(id -> presuntoPorEmpresa(id, anio))
        .sorted(Comparator.comparing(PresuntoEmpresaDto::getRazonSocial))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PresuntoEmpresaDto presuntoPorEmpresa(Long empresaId, Short anio) {
    List<PresuntoMensualItemDto> mensual = presuntoRepo
        .findAllByEmpresaIdAndAnioOrderByMesAsc(empresaId, anio)
        .stream()
        .map(p -> toPresuntoItemDto(p))
        .collect(Collectors.toList());

    BigDecimal total = presuntoRepo.sumAnualByEmpresa(empresaId, anio);

    String razonSocial = empresaRepo.findByIdAndDeletedAtIsNull(empresaId)
        .map(EmpresaEntity::getRazonSocial).orElse("Empresa " + empresaId);

    return PresuntoEmpresaDto.builder()
        .empresaId(empresaId)
        .razonSocial(razonSocial)
        .anio(anio)
        .totalPresuntoAnual(total)
        .mensual(mensual)
        .build();
  }

  @Override
  public int ejecutarPresuntoMensual(Short anio, Short mes) {
    return motor.ejecutar(anio, mes);
  }

  // ── helpers ──────────────────────────────────────────────────────

  private GmfEmpresaDto buildGmfEmpresaDto(Long empresaId, Short anio,
                                             List<GmfMovimientoEntity> movs) {
    BigDecimal total = movs.stream().map(GmfMovimientoEntity::getMontoGmf)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    String decision = movs.stream()
        .map(GmfMovimientoEntity::getDecisionAnual)
        .filter(d -> !"PENDIENTE".equals(d))
        .findFirst().orElse("PENDIENTE");

    String razonSocial = empresaRepo.findByIdAndDeletedAtIsNull(empresaId)
        .map(EmpresaEntity::getRazonSocial).orElse("Empresa " + empresaId);

    List<GmfMovimientoItemDto> items = movs.stream()
        .map(m -> {
          String ref = operacionRepo.findByIdAndDeletedAtIsNull(m.getOperacionId())
              .map(OperacionEntity::getReferencia).orElse(null);
          return GmfMovimientoItemDto.builder()
              .id(m.getId()).operacionId(m.getOperacionId()).referencia(ref)
              .anio(m.getAnio()).mes(m.getMes()).fecha(m.getFecha())
              .montoGmf(m.getMontoGmf()).decisionAnual(m.getDecisionAnual())
              .build();
        }).collect(Collectors.toList());

    return GmfEmpresaDto.builder()
        .empresaId(empresaId).razonSocial(razonSocial).anio(anio)
        .totalGmf(total).decisionAnual(decision).movimientos(items)
        .build();
  }

  private PresuntoMensualItemDto toPresuntoItemDto(InteresPresuntoMovimientoEntity p) {
    String ref = operacionRepo.findByIdAndDeletedAtIsNull(p.getOperacionId())
        .map(OperacionEntity::getReferencia).orElse(null);
    return PresuntoMensualItemDto.builder()
        .id(p.getId()).operacionId(p.getOperacionId()).referencia(ref)
        .mes(p.getMes()).saldoCapitalPromedio(p.getSaldoCapitalPromedio())
        .tasaPresuntaPorcentaje(p.getTasaPresuntaPorcentaje())
        .dias(p.getDias()).montoCalculado(p.getMontoCalculado())
        .build();
  }
}
