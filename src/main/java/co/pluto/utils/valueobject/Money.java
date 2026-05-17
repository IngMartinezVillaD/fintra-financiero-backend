package co.pluto.utils.valueobject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {

  private static final MathContext MC = MathContext.DECIMAL64;
  private static final int SCALE = 6;

  public Money {
    if (amount == null) throw new IllegalArgumentException("amount no puede ser null");
    if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency requerida");
    amount = amount.setScale(SCALE, RoundingMode.HALF_EVEN);
  }

  public static Money of(BigDecimal amount) { return new Money(amount, "COP"); }
  public static Money of(String amount)     { return of(new BigDecimal(amount)); }
  public static Money zero()                { return of(BigDecimal.ZERO); }

  public Money add(Money other) {
    assertSameCurrency(other);
    return new Money(amount.add(other.amount, MC), currency);
  }

  public Money subtract(Money other) {
    assertSameCurrency(other);
    return new Money(amount.subtract(other.amount, MC), currency);
  }

  public Money multiply(BigDecimal factor) {
    return new Money(amount.multiply(factor, MC), currency);
  }

  public boolean isPositive() { return amount.compareTo(BigDecimal.ZERO) > 0; }
  public boolean isZero()     { return amount.compareTo(BigDecimal.ZERO) == 0; }

  private void assertSameCurrency(Money other) {
    if (!currency.equals(other.currency))
      throw new IllegalArgumentException("No se pueden operar monedas distintas: " + currency + " vs " + other.currency);
  }
}
