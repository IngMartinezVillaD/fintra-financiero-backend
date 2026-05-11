package co.fintra.financiero.services.impl.seguimiento;

import java.math.BigDecimal;

public record TasaAplicable(BigDecimal tasaMensual, String tipoTasa) {

  public static TasaAplicable sinInteres() {
    return new TasaAplicable(BigDecimal.ZERO, "SIN_INTERES");
  }
}
