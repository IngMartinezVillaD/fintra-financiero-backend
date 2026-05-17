package co.pluto.dto.response.cupos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CupoRotativoResponseDto {
  private Long      id;
  private String    codigo;
  private Long      empresaId;
  private String    empresaNombre;
  private String    empresaNit;
  private String    tipoTasa;
  private BigDecimal tasaPorcentajeMensual;
  private BigDecimal valorCupo;
  private BigDecimal saldoDisponible;
  private String    estado;
  private String    observaciones;
  private Instant   createdAt;
}
