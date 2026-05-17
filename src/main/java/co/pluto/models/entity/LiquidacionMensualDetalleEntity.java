package co.pluto.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "prestamos", name = "liquidaciones_mensuales_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiquidacionMensualDetalleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "liquidacion_id", nullable = false)
  private Long liquidacionId;

  @Column(name = "operacion_id")
  private Long operacionId;

  @Column(name = "saldo_inicial_id")
  private Long saldoInicialId;

  @Column(name = "intereses_periodo", nullable = false, precision = 19, scale = 6)
  private BigDecimal interesesPeriodo;

  @Column(name = "retencion_fuente_aplicada", nullable = false, precision = 19, scale = 6)
  private BigDecimal retencionFuenteAplicada = BigDecimal.ZERO;

  @Column(name = "retencion_ica_aplicada", nullable = false, precision = 19, scale = 6)
  private BigDecimal retencionIcaAplicada = BigDecimal.ZERO;
}
