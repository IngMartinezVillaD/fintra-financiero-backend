package co.pluto.dto.response.saldos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class SaldoInicialResponseDto {
  private Long       id;
  private String     codigo;
  private Long       empresaPrestamistaId;
  private String     empresaPrestamistaNombre;
  private String     empresaPrestamistaNit;
  private Long       empresaPrestatariaId;
  private String     empresaPrestatariaNombre;
  private String     empresaPrestatariaNit;
  private String     tipoTasa;
  private BigDecimal tasaPorcentajeMensual;
  private BigDecimal saldoCapital;
  private BigDecimal interesesAcumulados;
  private LocalDate  fechaCorte;
  private String     estado;
  private String     observaciones;
  private Instant    createdAt;
}
