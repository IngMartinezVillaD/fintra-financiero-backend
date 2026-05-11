package co.fintra.financiero.services;

import co.fintra.financiero.models.entity.OperacionEntity;
import co.fintra.financiero.models.entity.TramoEntity;
import co.fintra.financiero.services.impl.seguimiento.AplicacionAbono;
import co.fintra.financiero.services.impl.seguimiento.MotorTramosService;
import co.fintra.financiero.services.impl.seguimiento.TasaAplicable;
import co.fintra.financiero.utils.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MotorTramosService — cálculos financieros")
class MotorTramosServiceTest {

  private MotorTramosService motor;

  @BeforeEach
  void setUp() {
    motor = new MotorTramosService();
  }

  // ── calcularInteres ──────────────────────────────────────────────

  @Nested
  @DisplayName("calcularInteres")
  class CalcularInteresTests {

    @Test
    @DisplayName("tasa 0 devuelve 0")
    void tasaCeroDevuelveCero() {
      BigDecimal result = motor.calcularInteres(
          new BigDecimal("100000000"), BigDecimal.ZERO, 30);
      assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest(name = "capital={0} tasa={1}% dias={2} → {3}")
    @CsvSource({
        "100000000, 1.0, 30, 1000000.00",  // 1% mensual × 30/30 = 1%
        "100000000, 1.0, 15, 500000.00",   // 1% mensual × 15/30 = 0.5%
        "50000000,  0.5, 30, 250000.00",   // 0.5% × 30/30
        "10000000,  2.0, 31, 206666.67",   // 2% × 31/30
    })
    @DisplayName("fórmula: capital × tasa × días/30")
    void calculoFormula(String capital, String tasa, int dias, String esperado) {
      BigDecimal result = motor.calcularInteres(
          new BigDecimal(capital), new BigDecimal(tasa), dias);
      assertThat(result).isEqualByComparingTo(new BigDecimal(esperado));
    }

    @Test
    @DisplayName("redondeo HALF_EVEN a 2 decimales")
    void redondeoHalfEven() {
      // capital=33333333 tasa=1% dias=30 → 333333.33
      BigDecimal result = motor.calcularInteres(
          new BigDecimal("33333333"), new BigDecimal("1.0"), 30);
      assertThat(result.scale()).isEqualTo(2);
    }
  }

  // ── cerrarTramoEnCurso ───────────────────────────────────────────

  @Nested
  @DisplayName("cerrarTramoEnCurso")
  class CerrarTramoTests {

    @Test
    @DisplayName("fecha_hasta = fechaCierre - 1, estado = LIQUIDADO")
    void cierraCorrectamente() {
      TramoEntity tramo = tramoBase(LocalDate.of(2026, 5, 1),
          new BigDecimal("100000000"), new BigDecimal("1.0"));
      motor.cerrarTramoEnCurso(tramo, LocalDate.of(2026, 5, 16), "LIQUIDACION_POR_ABONO");

      assertThat(tramo.getFechaHasta()).isEqualTo(LocalDate.of(2026, 5, 15));
      assertThat(tramo.getEstado()).isEqualTo("LIQUIDADO");
      assertThat(tramo.getDias()).isEqualTo(15); // 1–15 inclusive = 15 días
    }

    @Test
    @DisplayName("días inclusivos: mayo 1-31 = 31 días")
    void diasInclusivos() {
      TramoEntity tramo = tramoBase(LocalDate.of(2026, 5, 1),
          new BigDecimal("1000000"), new BigDecimal("1.0"));
      motor.cerrarTramoEnCurso(tramo, LocalDate.of(2026, 6, 1), "LIQUIDACION_CIERRE_MES");

      assertThat(tramo.getDias()).isEqualTo(31);
    }

    @Test
    @DisplayName("febrero bisiesto (2024) tiene 29 días")
    void febrerosBisiesto() {
      TramoEntity tramo = tramoBase(LocalDate.of(2024, 2, 1),
          new BigDecimal("1000000"), new BigDecimal("1.0"));
      motor.cerrarTramoEnCurso(tramo, LocalDate.of(2024, 3, 1), "LIQUIDACION_CIERRE_MES");

      assertThat(tramo.getDias()).isEqualTo(29);
    }

    @Test
    @DisplayName("tipoMovimiento se aplica al cerrar")
    void tipoMovimientoAplicado() {
      TramoEntity tramo = tramoBase(LocalDate.of(2026, 5, 1),
          new BigDecimal("1000000"), new BigDecimal("1.0"));
      motor.cerrarTramoEnCurso(tramo, LocalDate.of(2026, 5, 10), "LIQUIDACION_POR_ABONO");

      assertThat(tramo.getTipoMovimiento()).isEqualTo("LIQUIDACION_POR_ABONO");
    }
  }

  // ── abrirNuevoTramo ──────────────────────────────────────────────

  @Nested
  @DisplayName("abrirNuevoTramo")
  class AbrirTramoTests {

    @Test
    @DisplayName("tramo abre desde fechaInicio hasta fin de mes")
    void abreHastaFinMes() {
      OperacionEntity op = new OperacionEntity();
      TasaAplicable tasa = new TasaAplicable(new BigDecimal("1.0"), "COMERCIAL");

      TramoEntity nuevo = motor.abrirNuevoTramo(op, 2,
          LocalDate.of(2026, 5, 16), new BigDecimal("90000000"), tasa, "LIQUIDACION_POR_ABONO");

      assertThat(nuevo.getFechaDesde()).isEqualTo(LocalDate.of(2026, 5, 16));
      assertThat(nuevo.getFechaHasta()).isEqualTo(LocalDate.of(2026, 5, 31));
      assertThat(nuevo.getEstado()).isEqualTo("EN_CURSO");
      assertThat(nuevo.getNumeroTramo()).isEqualTo(2);
      assertThat(nuevo.getSaldoCapital()).isEqualByComparingTo(new BigDecimal("90000000"));
    }

    @Test
    @DisplayName("interés calculado correctamente para tramo parcial del mes")
    void interesTramoParcialmMes() {
      OperacionEntity op = new OperacionEntity();
      TasaAplicable tasa = new TasaAplicable(new BigDecimal("1.0"), "COMERCIAL");

      // Del 16 al 31 = 16 días
      TramoEntity nuevo = motor.abrirNuevoTramo(op, 2,
          LocalDate.of(2026, 5, 16), new BigDecimal("100000000"), tasa, "LIQUIDACION_POR_ABONO");

      // 100_000_000 × 1% × 16/30 = 533_333.33
      assertThat(nuevo.getInteresCalculado())
          .isEqualByComparingTo(new BigDecimal("533333.33"));
    }

    @Test
    @DisplayName("SIN_INTERES produce interés = 0")
    void sinInteresProduceCero() {
      OperacionEntity op = new OperacionEntity();
      TramoEntity nuevo = motor.abrirNuevoTramo(op, 1,
          LocalDate.of(2026, 5, 1), new BigDecimal("100000000"),
          TasaAplicable.sinInteres(), "DESEMBOLSO_INICIAL");

      assertThat(nuevo.getInteresCalculado()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(nuevo.getTipoTasa()).isEqualTo("SIN_INTERES");
    }
  }

  // ── aplicarAbono ─────────────────────────────────────────────────

  @Nested
  @DisplayName("aplicarAbono")
  class AplicarAbonoTests {

    @Test
    @DisplayName("orden correcto: intereses primero, luego capital")
    void interesesPrimeroLuegoCapital() {
      AplicacionAbono result = motor.aplicarAbono(
          new BigDecimal("500000"),    // interés causado
          new BigDecimal("10000000"),  // capital
          new BigDecimal("2000000")    // abono
      );

      assertThat(result.aplicadoAIntereses()).isEqualByComparingTo(new BigDecimal("500000"));
      assertThat(result.aplicadoACapital()).isEqualByComparingTo(new BigDecimal("1500000"));
      assertThat(result.nuevoSaldoCapital()).isEqualByComparingTo(new BigDecimal("8500000"));
      assertThat(result.interesesPendientes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("abono solo cubre intereses — capital intacto")
    void abonoSoloCubreIntereses() {
      AplicacionAbono result = motor.aplicarAbono(
          new BigDecimal("800000"),
          new BigDecimal("10000000"),
          new BigDecimal("500000")     // menor que intereses
      );

      assertThat(result.aplicadoAIntereses()).isEqualByComparingTo(new BigDecimal("500000"));
      assertThat(result.aplicadoACapital()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(result.nuevoSaldoCapital()).isEqualByComparingTo(new BigDecimal("10000000"));
      assertThat(result.interesesPendientes()).isEqualByComparingTo(new BigDecimal("300000"));
    }

    @Test
    @DisplayName("abono total saldo — capital queda en cero")
    void abonoTotalSaldaOperacion() {
      AplicacionAbono result = motor.aplicarAbono(
          new BigDecimal("200000"),
          new BigDecimal("5000000"),
          new BigDecimal("5200000")    // exactamente intereses + capital
      );

      assertThat(result.nuevoSaldoCapital()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(result.interesesPendientes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("abono mayor que deuda total lanza BusinessException")
    void abonoExcesoLanzaExcepcion() {
      assertThatThrownBy(() -> motor.aplicarAbono(
          new BigDecimal("100000"),
          new BigDecimal("5000000"),
          new BigDecimal("6000000")    // supera 5_100_000
      )).isInstanceOf(BusinessException.class)
        .hasMessageContaining("supera la deuda total");
    }

    @Test
    @DisplayName("operacion con cobra_interes=NO — abono solo aplica a capital")
    void sinInteresAbonoSoloCapital() {
      AplicacionAbono result = motor.aplicarAbono(
          BigDecimal.ZERO,             // sin intereses
          new BigDecimal("10000000"),
          new BigDecimal("3000000")
      );

      assertThat(result.aplicadoAIntereses()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(result.aplicadoACapital()).isEqualByComparingTo(new BigDecimal("3000000"));
      assertThat(result.nuevoSaldoCapital()).isEqualByComparingTo(new BigDecimal("7000000"));
    }

    @Test
    @DisplayName("dos abonos consecutivos producen saldos correctos")
    void dosAbonosConsecutivos() {
      // Abono 1
      AplicacionAbono a1 = motor.aplicarAbono(
          new BigDecimal("500000"), new BigDecimal("10000000"), new BigDecimal("2000000"));

      // Abono 2 con nuevo saldo
      AplicacionAbono a2 = motor.aplicarAbono(
          a1.interesesPendientes(), a1.nuevoSaldoCapital(), new BigDecimal("3000000"));

      assertThat(a2.nuevoSaldoCapital()).isEqualByComparingTo(new BigDecimal("5500000"));
    }
  }

  // ── calcularInteresEnCurso ───────────────────────────────────────

  @Nested
  @DisplayName("calcularInteresEnCurso")
  class InteresEnCursoTests {

    @Test
    @DisplayName("tramo null devuelve 0")
    void tramoNullDevuelveCero() {
      assertThat(motor.calcularInteresEnCurso(null, LocalDate.now()))
          .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("recalcula interés al día de hoy")
    void recalculaAHoy() {
      TramoEntity tramo = tramoBase(LocalDate.of(2026, 5, 1),
          new BigDecimal("100000000"), new BigDecimal("1.0"));
      // 1 día = 100_000_000 × 1% × 1/30 = 33_333.33
      BigDecimal result = motor.calcularInteresEnCurso(tramo, LocalDate.of(2026, 5, 1));
      assertThat(result).isEqualByComparingTo(new BigDecimal("33333.33"));
    }
  }

  // ── helpers ──────────────────────────────────────────────────────

  private TramoEntity tramoBase(LocalDate desde, BigDecimal capital, BigDecimal tasa) {
    return TramoEntity.builder()
        .numeroTramo(1)
        .tipoMovimiento("DESEMBOLSO_INICIAL")
        .fechaDesde(desde)
        .fechaHasta(desde.plusMonths(1).minusDays(1))
        .saldoCapital(capital)
        .tasaPorcentajeMensual(tasa)
        .tipoTasa("COMERCIAL")
        .interesCalculado(BigDecimal.ZERO)
        .estado("EN_CURSO")
        .dias(30)
        .version(0L)
        .build();
  }
}
