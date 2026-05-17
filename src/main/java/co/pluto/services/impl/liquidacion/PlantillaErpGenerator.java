package co.pluto.services.impl.liquidacion;

import co.pluto.models.entity.LiquidacionMensualDetalleEntity;
import co.pluto.models.entity.LiquidacionMensualEntity;

import java.util.List;

public interface PlantillaErpGenerator {

  String erpCodigo();

  PlantillaErpResult generar(LiquidacionMensualEntity liquidacion,
                              List<LiquidacionMensualDetalleEntity> detalles,
                              String empresaNombre);
}
