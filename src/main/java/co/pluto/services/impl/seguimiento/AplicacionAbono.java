package co.pluto.services.impl.seguimiento;

import java.math.BigDecimal;

public record AplicacionAbono(
    BigDecimal aplicadoAIntereses,
    BigDecimal aplicadoACapital,
    BigDecimal nuevoSaldoCapital,
    BigDecimal interesesPendientes
) {}
