package co.pluto.services.impl.bancos;

import org.springframework.stereotype.Component;

@Component
public class BancolombiaArchivoPlanoGenerator extends StubArchivoPlanoGenerator {
  @Override public String bancoCodigo() { return "001"; }
  @Override public String formato()     { return "BANCOLOMBIA_ACH"; }
}
