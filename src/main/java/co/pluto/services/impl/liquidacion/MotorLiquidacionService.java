package co.pluto.services.impl.liquidacion;

import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.services.impl.seguimiento.MotorTramosService;
import co.pluto.services.impl.seguimiento.ResolutorTasaAplicableService;
import co.pluto.services.impl.seguimiento.TasaAplicable;
import co.pluto.utils.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MotorLiquidacionService {

  private final IOperacionRepository      operacionRepo;
  private final ISaldoInicialRepository   saldoInicialRepo;
  private final ITramoRepository          tramoRepo;
  private final ILiquidacionMensualDetalleRepository detalleRepo;
  private final MotorTramosService        motor;
  private final ResolutorTasaAplicableService resolutor;

  /**
   * Ejecuta el cálculo para todas las operaciones vigentes (DS) con cobro de interés.
   * Idempotente: operaciones ya calculadas se saltan.
   */
  @Transactional
  public void calcular(LiquidacionMensualEntity liq) {
    List<OperacionEntity> operacionesVigentes = operacionRepo
        .findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS")
        .stream()
        .filter(op -> !"NO".equals(op.getCobraInteres()))
        .toList();

    BigDecimal totalIntereses = BigDecimal.ZERO;

    for (OperacionEntity op : operacionesVigentes) {
      if (detalleRepo.existsByLiquidacionIdAndOperacionId(liq.getId(), op.getId())) {
        // Ya calculado para esta operación — idempotente
        BigDecimal yaCalculado = detalleRepo
            .findByLiquidacionIdAndOperacionId(liq.getId(), op.getId())
            .map(LiquidacionMensualDetalleEntity::getInteresesPeriodo)
            .orElse(BigDecimal.ZERO);
        totalIntereses = totalIntereses.add(yaCalculado);
        continue;
      }

      try {
        BigDecimal interesesOp = calcularParaOperacion(op, liq);
        totalIntereses = totalIntereses.add(interesesOp);
      } catch (Exception e) {
        log.warn("Error calculando liquidación para operación {}: {}", op.getReferencia(), e.getMessage());
      }
    }

    // Saldos iniciales ACTIVOS con cobro de interés
    List<SaldoInicialEntity> saldosActivos = saldoInicialRepo.findAllByEstadoAndDeletedAtIsNull("ACTIVO")
        .stream()
        .filter(s -> !"ESPECIAL_SIN_INTERES".equals(s.getTipoTasa()))
        .toList();

    for (SaldoInicialEntity si : saldosActivos) {
      if (detalleRepo.existsByLiquidacionIdAndSaldoInicialId(liq.getId(), si.getId())) {
        BigDecimal yaCalculado = detalleRepo
            .findByLiquidacionIdAndSaldoInicialId(liq.getId(), si.getId())
            .map(LiquidacionMensualDetalleEntity::getInteresesPeriodo)
            .orElse(BigDecimal.ZERO);
        totalIntereses = totalIntereses.add(yaCalculado);
        continue;
      }
      try {
        BigDecimal interesesSi = calcularParaSaldoInicial(si, liq);
        totalIntereses = totalIntereses.add(interesesSi);
      } catch (Exception e) {
        log.warn("Error calculando liquidación para saldo inicial {}: {}", si.getCodigo(), e.getMessage());
      }
    }

    liq.setTotalInteresesLiquidados(totalIntereses);
  }

  private BigDecimal calcularParaSaldoInicial(SaldoInicialEntity si, LiquidacionMensualEntity liq) {
    // Interés mensual simple: capital × tasa%/100
    BigDecimal interesesPeriodo = si.getSaldoCapital()
        .multiply(si.getTasaPorcentajeMensual().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN))
        .setScale(6, RoundingMode.HALF_EVEN);

    EmpresaEntity prestataria = si.getEmpresaPrestataria();
    BigDecimal retFuente = calcularRetencion(interesesPeriodo, prestataria.getRetencionFuentePorcentaje());
    BigDecimal retIca    = calcularRetencion(interesesPeriodo, prestataria.getRetencionIcaPorcentaje());

    detalleRepo.save(LiquidacionMensualDetalleEntity.builder()
        .liquidacionId(liq.getId())
        .saldoInicialId(si.getId())
        .interesesPeriodo(interesesPeriodo)
        .retencionFuenteAplicada(retFuente)
        .retencionIcaAplicada(retIca)
        .build());

    return interesesPeriodo;
  }

  private BigDecimal calcularParaOperacion(OperacionEntity op, LiquidacionMensualEntity liq) {
    TramoEntity tramoActual = tramoRepo
        .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(op.getId(), "EN_CURSO")
        .orElse(null);

    BigDecimal interesesPeriodo = BigDecimal.ZERO;

    if (tramoActual != null) {
      // Cerrar el tramo al fecha de corte
      motor.cerrarTramoEnCurso(tramoActual, liq.getFechaCorte().plusDays(1), "LIQUIDACION_CIERRE_MES");
      tramoActual.setLiquidacionId(liq.getId());
      tramoRepo.save(tramoActual);

      interesesPeriodo = tramoActual.getInteresCalculado();

      // Abrir tramo del siguiente período (falla silenciosa si no hay tasa configurada)
      try {
        TasaAplicable tasa = resolutor.resolver(op, liq.getFechaCorte().plusDays(1));
        int nuevoNumero = tramoRepo.maxNumeroTramo(op.getId()) + 1;
        TramoEntity nuevoTramo = motor.abrirNuevoTramo(op, nuevoNumero,
            liq.getFechaCorte().plusDays(1), tramoActual.getSaldoCapital(),
            tasa, "LIQUIDACION_CIERRE_MES");
        tramoRepo.save(nuevoTramo);
      } catch (Exception e) {
        log.warn("No se pudo abrir tramo siguiente para operación {} — tasa no configurada para el período siguiente: {}",
            op.getReferencia(), e.getMessage());
      }
    } else {
      // Sin tramo en curso: sumar tramos liquidados del período
      interesesPeriodo = tramoRepo.sumInteresLiquidado(op.getId());
    }

    // Calcular retenciones
    EmpresaEntity prestataria = op.getEmpresaPrestataria();
    BigDecimal retFuente = calcularRetencion(interesesPeriodo, prestataria.getRetencionFuentePorcentaje());
    BigDecimal retIca    = calcularRetencion(interesesPeriodo, prestataria.getRetencionIcaPorcentaje());

    detalleRepo.save(LiquidacionMensualDetalleEntity.builder()
        .liquidacionId(liq.getId())
        .operacionId(op.getId())
        .interesesPeriodo(interesesPeriodo)
        .retencionFuenteAplicada(retFuente)
        .retencionIcaAplicada(retIca)
        .build());

    return interesesPeriodo;
  }

  /**
   * Revierte el cálculo: reabre tramos cerrados, elimina detalles.
   */
  @Transactional
  public void revertir(LiquidacionMensualEntity liq) {
    // Reabrir tramos cerrados por esta liquidación
    List<LiquidacionMensualDetalleEntity> detalles = detalleRepo.findAllByLiquidacionId(liq.getId());

    for (LiquidacionMensualDetalleEntity detalle : detalles) {
      // Tramo cerrado (tiene liquidacion_id = liq.id)
      tramoRepo.findAllByOperacionIdAndDeletedAtIsNullOrderByFechaDesdeAsc(detalle.getOperacionId())
          .stream()
          .filter(t -> liq.getId().equals(t.getLiquidacionId()))
          .forEach(t -> {
            t.setEstado("EN_CURSO");
            t.setLiquidacionId(null);
            t.setFechaHasta(YearMonth.now().atEndOfMonth());
            t.setDias(Math.max(1, (int) ChronoUnit.DAYS.between(t.getFechaDesde(), t.getFechaHasta()) + 1));
            tramoRepo.save(t);
          });

      // Eliminar tramo abierto para el siguiente período (fecha_desde = fechaCorte + 1)
      tramoRepo.findAllByOperacionIdAndDeletedAtIsNullOrderByFechaDesdeAsc(detalle.getOperacionId())
          .stream()
          .filter(t -> "EN_CURSO".equals(t.getEstado())
              && t.getFechaDesde().equals(liq.getFechaCorte().plusDays(1)))
          .forEach(t -> t.setDeletedAt(java.time.OffsetDateTime.now()));
    }

    detalleRepo.deleteAllByLiquidacionId(liq.getId());
    liq.setTotalInteresesLiquidados(BigDecimal.ZERO);
  }

  private BigDecimal calcularRetencion(BigDecimal base, BigDecimal porcentaje) {
    if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return base.multiply(porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN))
        .setScale(6, RoundingMode.HALF_EVEN);
  }
}
