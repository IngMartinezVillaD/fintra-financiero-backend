package co.fintra.financiero.services.impl.liquidacion;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class PlantillaErpResult {
  String contenido;
  String nombreArchivo;
  String formatoMime;
}
