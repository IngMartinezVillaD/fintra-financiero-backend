package co.fintra.financiero.services.impl.controles;

import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

/**
 * Calcula el interés presunto fiscal (extracontable, nunca genera asiento).
 * Aplica cuando la empresa tiene calcula_interes_presunto=true y cobra_interes=NO.
 * Tasa presunta configurable (default: tasa DIAN vigente ~0.40% mensual).
 * Pendiente confirmación de Tesorería para operaciones con tasa inferior a presunta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MotorInteresPresuntoService {

  @Value("${fintra.tasa-presunta-mensual-porcentaje:0.40}")
  private BigDecimal tasaPresuntaMensual;

  private final IOperacionRepository       operacionRepo;
  private final ITramoRepository           tramoRepo;
  private final IInteresPresuntoRepository  presuntoRepo;

  @Transactional
  public int ejecutar(Short anio, Short mes) {
    YearMonth ym = YearMonth.of(anio, mes);
    int diasMes = ym.lengthOfMonth();
    int procesados = 0;

    for (OperacionEntity op : operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS")) {
      if (!"NO".equals(op.getCobraInteres())) continue;

      EmpresaEntity prestamista = op.getEmpresaPrestamista();
      EmpresaEntity prestataria  = op.getEmpresaPrestataria();

      if (!Boolean.TRUE.equals(prestamista.getCalculaInteresPresunto())
          && !Boolean.TRUE.equals(prestataria.getCalculaInteresPresunto())) continue;

      TramoEntity tramo = tramoRepo
          .findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(op.getId(), "EN_CURSO")
          .orElse(null);
      if (tramo == null) continue;

      BigDecimal saldoCapital = tramo.getSaldoCapital();
      BigDecimal monto = saldoCapital
          .multiply(tasaPresuntaMensual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN))
          .multiply(BigDecimal.valueOf(diasMes).divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_EVEN))
          .setScale(6, RoundingMode.HALF_EVEN);

      // Registrar por empresa con el flag activo
      Long empresaId = Boolean.TRUE.equals(prestamista.getCalculaInteresPresunto())
          ? prestamista.getId() : prestataria.getId();

      if (presuntoRepo.existsByEmpresaIdAndOperacionIdAndAnioAndMes(empresaId, op.getId(), anio, mes)) {
        continue;
      }

      presuntoRepo.save(InteresPresuntoMovimientoEntity.builder()
          .empresaId(empresaId)
          .operacionId(op.getId())
          .anio(anio)
          .mes(mes)
          .saldoCapitalPromedio(saldoCapital)
          .tasaPresuntaPorcentaje(tasaPresuntaMensual)
          .dias(diasMes)
          .montoCalculado(monto)
          .build());
      procesados++;
    }

    log.info("Interés presunto ejecutado {}/{}: {} registros generados", mes, anio, procesados);
    return procesados;
  }
}
