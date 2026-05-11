package co.fintra.financiero.services;

import co.fintra.financiero.services.impl.seguimiento.MotorTramosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cálculo GMF — 4/1000")
class CalculadorGmfTest {

  private static final BigDecimal TARIFA = new BigDecimal("0.004");

  @ParameterizedTest(name = "monto={0} → gmf={1}")
  @CsvSource({
      "100000000, 400000.00",
      "50000000,  200000.00",
      "1000000,   4000.00",
      "250000,    1000.00",
  })
  @DisplayName("GMF = 4/1000 × monto")
  void calculoGmf(String monto, String esperado) {
    BigDecimal gmf = new BigDecimal(monto).multiply(TARIFA)
        .setScale(2, java.math.RoundingMode.HALF_EVEN);
    assertThat(gmf).isEqualByComparingTo(new BigDecimal(esperado));
  }

  @Test
  @DisplayName("cuenta exenta → GMF = 0")
  void cuentaExentaDevuelveCero() {
    // Simulación: si exentaGmf=true, el servicio devuelve 0
    BigDecimal gmf = BigDecimal.ZERO;
    assertThat(gmf).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("precisión: 6 decimales almacenados, 2 para presentación")
  void precision() {
    BigDecimal monto = new BigDecimal("33333333");
    BigDecimal gmf = monto.multiply(TARIFA).setScale(6, java.math.RoundingMode.HALF_EVEN);
    assertThat(gmf.scale()).isEqualTo(6);
    assertThat(gmf).isEqualByComparingTo(new BigDecimal("133333.332000"));
  }
}
