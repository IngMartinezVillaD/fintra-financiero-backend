package co.fintra.financiero.services.impl.liquidacion;

import co.fintra.financiero.models.entity.LiquidacionMensualDetalleEntity;
import co.fintra.financiero.models.entity.LiquidacionMensualEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generador stub para SIIGO ERP (formatos EMP-03 y EMP-04).
 * Estructura completa pendiente de especificación de Contabilidad.
 */
@Component
public class SiigoPlantillaGenerator implements PlantillaErpGenerator {

  @Override
  public String erpCodigo() { return "SIIGO"; }

  @Override
  public PlantillaErpResult generar(LiquidacionMensualEntity liq,
                                     List<LiquidacionMensualDetalleEntity> detalles,
                                     String empresaNombre) {
    StringBuilder sb = new StringBuilder();
    sb.append("# SIIGO ERP — Formato EMP-03/EMP-04 pendiente de especificación\n");
    sb.append("TIPO;EMPRESA;OPERACION;INTERESES;RETENCION_FUENTE;RETENCION_ICA\n");

    for (LiquidacionMensualDetalleEntity d : detalles) {
      sb.append("LIQ-").append(liq.getAnio()).append(String.format("%02d", liq.getMes()))
        .append("-").append(d.getOperacionId()).append(";")
        .append(empresaNombre).append(";")
        .append(d.getOperacionId()).append(";")
        .append(d.getInteresesPeriodo()).append(";")
        .append(d.getRetencionFuenteAplicada()).append(";")
        .append(d.getRetencionIcaAplicada()).append("\n");
    }

    return PlantillaErpResult.builder()
        .contenido(sb.toString())
        .nombreArchivo("siigo-" + liq.getAnio() + "-" + String.format("%02d", liq.getMes()) + ".csv")
        .formatoMime("text/csv")
        .build();
  }
}
