package co.fintra.financiero.services.impl.bancos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ArchivoPlanoGeneratorRegistry {

  private final Map<String, ArchivoPlanoGenerator> generatorsByBanco;

  public ArchivoPlanoGeneratorRegistry(List<ArchivoPlanoGenerator> generators) {
    this.generatorsByBanco = generators.stream()
        .collect(Collectors.toMap(ArchivoPlanoGenerator::bancoCodigo, Function.identity()));
  }

  public ArchivoPlanoGenerator getForBanco(String bancoCodigo) {
    ArchivoPlanoGenerator gen = generatorsByBanco.get(bancoCodigo);
    if (gen == null) {
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
          "Formato de archivo plano pendiente de definición para banco código: " + bancoCodigo);
    }
    return gen;
  }
}
