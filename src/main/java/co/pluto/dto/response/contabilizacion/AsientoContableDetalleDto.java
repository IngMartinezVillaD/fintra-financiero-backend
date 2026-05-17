package co.pluto.dto.response.contabilizacion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class AsientoContableDetalleDto {
  private Short      orden;
  private Long       cuentaPucId;
  private String     cuentaCodigo;
  private String     cuentaNombre;
  private String     naturaleza;
  private BigDecimal monto;
  private String     glosa;
}
