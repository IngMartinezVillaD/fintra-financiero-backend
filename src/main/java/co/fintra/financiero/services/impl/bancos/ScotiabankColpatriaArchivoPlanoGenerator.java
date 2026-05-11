package co.fintra.financiero.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class ScotiabankColpatriaArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "032"; }
  @Override public String formato()     { return "SCOTIABANK_ACH"; }
}
