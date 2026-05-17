package co.pluto.services.impl.liquidacion;

import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Motor de liquidación diaria.
 *
 * Calcula 1 día de interés para cada operación DS activa y cada saldo inicial ACTIVO
 * usando la fórmula: capital × (tasaPorcentajeMensual / 100) / 30.
 *
 * NO cierra ni reabre tramos — solo lee el tramo EN_CURSO para obtener
 * el saldo de capital y la tasa vigente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MotorLiquidacionDiariaService {

  private static final BigDecimal CIEN      = BigDecimal.valueOf(100);
  private static final BigDecimal DIAS_MES  = BigDecimal.valueOf(30);

  private final IOperacionRepository                operacionRepo;
  private final ISaldoInicialRepository             saldoInicialRepo;
  private final ITramoRepository                    tramoRepo;
  private final ILiquidacionDiariaDetalleRepository detalleRepo;

  /**
   * Calcula los intereses del día para todas las operaciones y saldos iniciales vigentes.
   * Idempotente: registros ya calculados se saltan.
   */
  @Transactional
  public void calcular(LiquidacionDiariaEntity liq) {
    List<OperacionEntity> operacionesVigentes = operacionRepo
        .findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS")
        .stream()
        .filter(op -> !"NO".equals(op.getCobraInteres()))
        .toList();

    BigDecimal totalIntereses = BigDecimal.ZERO;

    for (OperacionEntity op : operacionesVigentes) {
      if (detalleRepo.existsByLiquidacionIdAndOperacionId(liq.getId(), op.getId())) {
        BigDecimal yaCalculado = detalleRepo.findAllByLiquidacionId(liq.getId())
            .stream()
            .filter(d -> op.getId().equals(d.getOperacionId()))
            .map(LiquidacionDiariaDetalleEntity::getInteresesPeriodo)
            .findFirst()
            .orElse(BigDecimal.ZERO);
        totalIntereses = totalIntereses.add(yaCalculado);
        continue;
      }

      try {
        BigDecimal interesesOp = calcularParaOperacion(op, liq);
        totalIntereses = totalIntereses.add(interesesOp);
      } catch (Exception e) {
        log.warn("Error calculando liquidación diaria para operación {}: {}",
            op.getReferencia(), e.getMessage());
      }
    }

    // Saldos iniciales ACTIVOS con cobro de interés
    List<SaldoInicialEntity> saldosActivos = saldoInicialRepo
        .findAllByEstadoAndDeletedAtIsNull("ACTIVO")
        .stream()
        .filter(s -> !"ESPECIAL_SIN_INTERES".equals(s.getTipoTasa()))
        .toList();

    for (SaldoInicialEntity si : saldosActivos) {
      if (detalleRepo.existsByLiquidacionIdAndSaldoInicialId(liq.getId(), si.getId())) {
        BigDecimal yaCalculado = detalleRepo.findAllByLiquidacionId(liq.getId())
            .stream()
            .filter(d -> si.getId().equals(d.getSaldoInicialId()))
            .map(LiquidacionDiariaDetalleEntity::getInteresesPeriodo)
            .findFirst()
            .orElse(BigDecimal.ZERO);
        totalIntereses = totalIntereses.add(yaCalculado);
        continue;
      }

      try {
        BigDecimal interesesSi = calcularParaSaldoInicial(si, liq);
        totalIntereses = totalIntereses.add(interesesSi);
      } catch (Exception e) {
        log.warn("Error calculando liquidación diaria para saldo inicial {}: {}",
            si.getCodigo(), e.getMessage());
      }
    }

    liq.setTotalInteresesLiquidados(totalIntereses);
  }

  /**
   * Revierte el cálculo eliminando los detalles.
   * NO manipula tramos (diferencia clave respecto a la liquidación mensual).
   */
  @Transactional
  public void revertir(LiquidacionDiariaEntity liq) {
    detalleRepo.deleteAllByLiquidacionId(liq.getId());
    liq.setTotalInteresesLiquidados(BigDecimal.ZERO);
  }

  // ── privados ────────────────────────────────────────────────────────────────

  private BigDecimal calcularParaOperacion(OperacionEntity op, LiquidacionDiariaEntity liq) {
    // Primero busca EN_CURSO; si la mensual ya cerró el tramo sin abrir uno nuevo,
    // cae al último LIQUIDADO para conservar capital y tasa vigentes.
    TramoEntity tramoActual = tramoRepo
        .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(op.getId(), "EN_CURSO")
        .orElseGet(() -> tramoRepo
            .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(op.getId(), "LIQUIDADO")
            .orElse(null));

    if (tramoActual == null) {
      log.debug("Operación {} sin tramo activo — se omite en liquidación diaria", op.getReferencia());
      return BigDecimal.ZERO;
    }

    // interesDia = capital × (tasa% / 100) / 30
    BigDecimal interesDia = interesDiario(tramoActual.getSaldoCapital(), tramoActual.getTasaPorcentajeMensual());

    EmpresaEntity prestataria = op.getEmpresaPrestataria();
    BigDecimal retFuente = calcularRetencion(interesDia, prestataria.getRetencionFuentePorcentaje());
    BigDecimal retIca    = calcularRetencion(interesDia, prestataria.getRetencionIcaPorcentaje());

    detalleRepo.save(LiquidacionDiariaDetalleEntity.builder()
        .liquidacionId(liq.getId())
        .operacionId(op.getId())
        .interesesPeriodo(interesDia)
        .retencionFuenteAplicada(retFuente)
        .retencionIcaAplicada(retIca)
        .build());

    return interesDia;
  }

  private BigDecimal calcularParaSaldoInicial(SaldoInicialEntity si, LiquidacionDiariaEntity liq) {
    // interesDia = capital × (tasa% / 100) / 30
    BigDecimal interesDia = interesDiario(si.getSaldoCapital(), si.getTasaPorcentajeMensual());

    EmpresaEntity prestataria = si.getEmpresaPrestataria();
    BigDecimal retFuente = calcularRetencion(interesDia, prestataria.getRetencionFuentePorcentaje());
    BigDecimal retIca    = calcularRetencion(interesDia, prestataria.getRetencionIcaPorcentaje());

    detalleRepo.save(LiquidacionDiariaDetalleEntity.builder()
        .liquidacionId(liq.getId())
        .saldoInicialId(si.getId())
        .interesesPeriodo(interesDia)
        .retencionFuenteAplicada(retFuente)
        .retencionIcaAplicada(retIca)
        .build());

    return interesDia;
  }

  private BigDecimal interesDiario(BigDecimal capital, BigDecimal tasaMensual) {
    return capital
        .multiply(tasaMensual.divide(CIEN, 10, RoundingMode.HALF_EVEN))
        .divide(DIAS_MES, 6, RoundingMode.HALF_EVEN);
  }

  private BigDecimal calcularRetencion(BigDecimal base, BigDecimal porcentaje) {
    if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return base.multiply(porcentaje.divide(CIEN, 10, RoundingMode.HALF_EVEN))
        .setScale(6, RoundingMode.HALF_EVEN);
  }
}
