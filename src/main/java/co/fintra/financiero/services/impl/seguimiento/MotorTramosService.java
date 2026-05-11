package co.fintra.financiero.services.impl.seguimiento;

import co.fintra.financiero.models.entity.OperacionEntity;
import co.fintra.financiero.models.entity.TramoEntity;
import co.fintra.financiero.utils.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Motor de cálculo de tramos: lógica financiera pura.
 * Días inclusivos según contabilidad colombiana (ambos extremos cuentan).
 * Interés: saldoCapital × (tasaMensual/100) × (dias/30), redondeado HALF_EVEN a 2 decimales.
 */
@Component
public class MotorTramosService {

  private static final BigDecimal TREINTA = BigDecimal.valueOf(30);
  private static final BigDecimal CIEN    = BigDecimal.valueOf(100);

  /**
   * Cierra el tramo en curso hasta fechaCierre (exclusiva).
   * fecha_hasta = fechaCierre - 1 día. dias = (fecha_hasta - fecha_desde) + 1 (inclusivo).
   */
  public TramoEntity cerrarTramoEnCurso(TramoEntity enCurso, LocalDate fechaCierre, String tipoMovimiento) {
    LocalDate fechaHasta = fechaCierre.minusDays(1);
    if (!fechaHasta.isAfter(enCurso.getFechaDesde().minusDays(1))) {
      fechaHasta = enCurso.getFechaDesde(); // mínimo 1 día
    }
    int dias = Math.max(1, (int) ChronoUnit.DAYS.between(enCurso.getFechaDesde(), fechaHasta) + 1);

    BigDecimal interes = calcularInteres(enCurso.getSaldoCapital(), enCurso.getTasaPorcentajeMensual(), dias);

    enCurso.setFechaHasta(fechaHasta);
    enCurso.setDias(dias);
    enCurso.setInteresCalculado(interes);
    enCurso.setTipoMovimiento(tipoMovimiento);
    enCurso.setEstado("LIQUIDADO");
    return enCurso;
  }

  /**
   * Abre un nuevo tramo desde fechaInicio hasta fin del mes, con la tasa dada.
   */
  public TramoEntity abrirNuevoTramo(OperacionEntity op, int nuevoNumero,
                                      LocalDate fechaInicio, BigDecimal saldoCapital,
                                      TasaAplicable tasa, String tipoMovimiento) {
    LocalDate fechaHasta = YearMonth.from(fechaInicio).atEndOfMonth();
    int dias = Math.max(1, (int) ChronoUnit.DAYS.between(fechaInicio, fechaHasta) + 1);
    BigDecimal interes = calcularInteres(saldoCapital, tasa.tasaMensual(), dias);

    return TramoEntity.builder()
        .operacion(op)
        .numeroTramo(nuevoNumero)
        .tipoMovimiento(tipoMovimiento)
        .fechaDesde(fechaInicio)
        .fechaHasta(fechaHasta)
        .saldoCapital(saldoCapital)
        .dias(dias)
        .tasaPorcentajeMensual(tasa.tasaMensual())
        .tipoTasa(tasa.tipoTasa())
        .interesCalculado(interes)
        .estado("EN_CURSO")
        .build();
  }

  /**
   * Aplica un abono: 1° intereses causados, 2° capital.
   * Lanza BusinessException si el abono excede la deuda total.
   */
  public AplicacionAbono aplicarAbono(BigDecimal interesCausadoAcumulado,
                                       BigDecimal saldoCapital,
                                       BigDecimal montoAbono) {
    BigDecimal deudaTotal = interesCausadoAcumulado.add(saldoCapital);
    if (montoAbono.compareTo(deudaTotal) > 0) {
      throw new BusinessException(
          String.format("El abono (%.2f) supera la deuda total (%.2f = %.2f intereses + %.2f capital)",
              montoAbono, deudaTotal, interesCausadoAcumulado, saldoCapital));
    }

    BigDecimal aplicadoAIntereses = montoAbono.min(interesCausadoAcumulado);
    BigDecimal restante           = montoAbono.subtract(aplicadoAIntereses);
    BigDecimal aplicadoACapital   = restante.min(saldoCapital);
    BigDecimal nuevoSaldo         = saldoCapital.subtract(aplicadoACapital).setScale(6, RoundingMode.HALF_EVEN);
    BigDecimal interesesPendientes = interesCausadoAcumulado.subtract(aplicadoAIntereses).setScale(6, RoundingMode.HALF_EVEN);

    return new AplicacionAbono(aplicadoAIntereses, aplicadoACapital, nuevoSaldo, interesesPendientes);
  }

  /**
   * Recalcula el interés en curso del tramo activo hasta hoy (para visualización en vivo).
   */
  public BigDecimal calcularInteresEnCurso(TramoEntity tramoActivo, LocalDate hoy) {
    if (tramoActivo == null) return BigDecimal.ZERO;
    LocalDate desde = tramoActivo.getFechaDesde();
    if (hoy.isBefore(desde)) return BigDecimal.ZERO;
    int dias = Math.max(1, (int) ChronoUnit.DAYS.between(desde, hoy) + 1);
    return calcularInteres(tramoActivo.getSaldoCapital(), tramoActivo.getTasaPorcentajeMensual(), dias);
  }

  public BigDecimal calcularInteres(BigDecimal saldoCapital, BigDecimal tasaMensual, int dias) {
    if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return saldoCapital
        .multiply(tasaMensual.divide(CIEN, 10, RoundingMode.HALF_EVEN))
        .multiply(BigDecimal.valueOf(dias).divide(TREINTA, 10, RoundingMode.HALF_EVEN))
        .setScale(2, RoundingMode.HALF_EVEN);
  }
}
