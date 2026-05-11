package co.fintra.financiero.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class DaviviendaArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "006"; }
  @Override public String formato()     { return "DAVIVIENDA_ACH"; }
}
