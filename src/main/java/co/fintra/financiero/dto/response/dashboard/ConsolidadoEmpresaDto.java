package co.fintra.financiero.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class ConsolidadoEmpresaDto {
  private Long empresaId;
  private String codigoInterno;
  private String razonSocial;
  private String nit;
  private Long totalOperaciones;
  private BigDecimal saldoCapitalVigente;
  private BigDecimal interesesCausadosPendientes;
  private BigDecimal totalDesembolsado;
  private LocalDate ultimoDesembolsoFecha;
}
