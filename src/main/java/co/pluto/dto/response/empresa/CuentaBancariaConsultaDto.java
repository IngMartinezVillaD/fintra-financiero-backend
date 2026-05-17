package co.pluto.dto.response.empresa;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CuentaBancariaConsultaDto {
  private Long   id;
  private Long   empresaId;
  private String empresaCodigo;
  private String empresaNombre;
  private String bancoCodigo;
  private String bancoNombre;
  private String tipo;
  private String numeroCuenta;
  private String titular;
  private String codigoContable;
  private Boolean exentaGmf;
  private Boolean activa;
}
