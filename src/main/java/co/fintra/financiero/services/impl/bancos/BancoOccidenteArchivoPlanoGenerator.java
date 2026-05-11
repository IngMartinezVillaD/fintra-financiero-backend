package co.fintra.financiero.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class BancoOccidenteArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "023"; }
  @Override public String formato()     { return "OCCIDENTE_ACH"; }
}
