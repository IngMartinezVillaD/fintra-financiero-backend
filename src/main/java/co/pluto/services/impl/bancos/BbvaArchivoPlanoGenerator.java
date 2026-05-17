package co.pluto.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class BbvaArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "013"; }
  @Override public String formato()     { return "BBVA_ACH"; }
}
