package co.fintra.financiero.services.impl.liquidacion;

import co.fintra.financiero.models.entity.LiquidacionMensualDetalleEntity;
import co.fintra.financiero.models.entity.LiquidacionMensualEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generador stub para Apotheosys ERP.
 * Estructura completa pendiente de especificación de Contabilidad.
 * Campo 27 (observación): "Liquidación intereses {mes} {año}" — inmutable.
 */
@Component
public class ApotheosysPlantillaGenerator implements PlantillaErpGenerator {

  @Override
  public String erpCodigo() { return "APOTHEOSYS"; }

  @Override
  public PlantillaErpResult generar(LiquidacionMensualEntity liq,
                                     List<LiquidacionMensualDetalleEntity> detalles,
                                     String empresaNombre) {
    String campo27 = observacionCampo27(liq);
    StringBuilder sb = new StringBuilder();
    sb.append("# APOTHEOSYS ERP — Formato pendiente de especificación\n");
    sb.append("# Campo 27 (observación): ").append(campo27).append("\n");
    sb.append("OPERACION_ID;INTERESES;RETENCION_FUENTE;RETENCION_ICA;NETO\n");

    for (LiquidacionMensualDetalleEntity d : detalles) {
      sb.append(d.getOperacionId()).append(";")
        .append(d.getInteresesPeriodo()).append(";")
        .append(d.getRetencionFuenteAplicada()).append(";")
        .append(d.getRetencionIcaAplicada()).append(";")
        .append(d.getInteresesPeriodo()
            .subtract(d.getRetencionFuenteAplicada())
            .subtract(d.getRetencionIcaAplicada()))
        .append("\n");
    }

    return PlantillaErpResult.builder()
        .contenido(sb.toString())
        .nombreArchivo("apotheosys-" + liq.getAnio() + "-" + String.format("%02d", liq.getMes()) + ".csv")
        .formatoMime("text/csv")
        .build();
  }

  private String observacionCampo27(LiquidacionMensualEntity liq) {
    String[] meses = {"", "enero","febrero","marzo","abril","mayo","junio",
                      "julio","agosto","septiembre","octubre","noviembre","diciembre"};
    return "Liquidación intereses " + meses[liq.getMes()] + " " + liq.getAnio();
  }
}
