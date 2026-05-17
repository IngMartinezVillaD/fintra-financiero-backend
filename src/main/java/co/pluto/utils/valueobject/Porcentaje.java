package co.pluto.utils.valueobject;

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

  /** Convierte tasa Efectiva Anual → Efectiva Mensual: EM = ((1 + EA/100)^(1/12) − 1) × 100 */
  public static BigDecimal eaToEm(BigDecimal ea) {
    double eaD = ea.doubleValue() / 100.0;
    double emD = (Math.pow(1.0 + eaD, 1.0 / 12.0) - 1.0) * 100.0;
    return BigDecimal.valueOf(emD).setScale(4, RoundingMode.HALF_EVEN);
  }

  /** Convierte tasa Efectiva Mensual → Efectiva Anual: EA = ((1 + EM/100)^12 − 1) × 100 */
  public static BigDecimal emToEa(BigDecimal em) {
    double emD = em.doubleValue() / 100.0;
    double eaD = (Math.pow(1.0 + emD, 12.0) - 1.0) * 100.0;
    return BigDecimal.valueOf(eaD).setScale(4, RoundingMode.HALF_EVEN);
  }
}
