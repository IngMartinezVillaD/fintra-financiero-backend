package co.fintra.financiero.dto.response.empresa;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CuentaBancariaResponseDto {
  private Long id;
  private String bancoCodigo;
  private String bancoNombre;
  private String tipo;
  private String numeroCuenta;
  private String titular;
  private String codigoContable;
  private String formatoArchivoPlano;
  private Boolean exentaGmf;
  private Boolean activa;
}
