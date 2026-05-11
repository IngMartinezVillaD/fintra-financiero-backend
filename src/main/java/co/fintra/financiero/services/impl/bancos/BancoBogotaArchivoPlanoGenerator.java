package co.fintra.financiero.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class BancoBogotaArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "002"; }
  @Override public String formato()     { return "BOGOTA_ACH"; }
}
