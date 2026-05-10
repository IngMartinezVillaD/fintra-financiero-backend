package co.fintra.financiero.utils.valueobject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public record Porcentaje(BigDecimal valor) {

  private static final MathContext MC = MathContext.DECIMAL64;

  public Porcentaje {
    if (valor == null) throw new IllegalArgumentException("valor no puede ser null");
    if (valor.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Porcentaje no puede ser negativo");
    valor = valor.setScale(4, RoundingMode.HALF_EVEN);
  }

  public static Porcentaje of(String valor)     { return new Porcentaje(new BigDecimal(valor)); }
  public static Porcentaje of(BigDecimal valor) { return new Porcentaje(valor); }

  public BigDecimal aDecimal() {
    return valor.divide(BigDecimal.valueOf(100), MC);
  }

  public Money aplicarA(Money capital) {
    return capital.multiply(aDecimal());
  }
}
