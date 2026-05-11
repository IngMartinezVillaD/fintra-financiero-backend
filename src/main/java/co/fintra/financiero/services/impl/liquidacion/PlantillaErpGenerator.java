package co.fintra.financiero.services.impl.liquidacion;

import co.fintra.financiero.models.entity.LiquidacionMensualDetalleEntity;
import co.fintra.financiero.models.entity.LiquidacionMensualEntity;

import java.util.List;

public interface PlantillaErpGenerator {

  String erpCodigo();

  PlantillaErpResult generar(LiquidacionMensualEntity liquidacion,
                              List<LiquidacionMensualDetalleEntity> detalles,
                              String empresaNombre);
}
