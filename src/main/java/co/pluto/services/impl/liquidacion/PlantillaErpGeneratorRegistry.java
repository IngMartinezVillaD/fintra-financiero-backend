package co.pluto.services.impl.liquidacion;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PlantillaErpGeneratorRegistry {

  private final Map<String, PlantillaErpGenerator> byErp;

  public PlantillaErpGeneratorRegistry(List<PlantillaErpGenerator> generators) {
    this.byErp = generators.stream()
        .collect(Collectors.toMap(PlantillaErpGenerator::erpCodigo, Function.identity()));
  }

  public PlantillaErpGenerator getForErp(String erpCodigo) {
    if (erpCodigo == null) return byErp.get("APOTHEOSYS");
    PlantillaErpGenerator gen = byErp.get(erpCodigo.toUpperCase());
    if (gen == null)
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
          "Generador de plantilla no disponible para ERP: " + erpCodigo);
    return gen;
  }
}
