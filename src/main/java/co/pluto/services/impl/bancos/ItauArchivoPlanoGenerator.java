package co.pluto.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class ItauArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "014"; }
  @Override public String formato()     { return "ITAU_ACH"; }
}
