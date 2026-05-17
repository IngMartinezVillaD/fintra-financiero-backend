package co.pluto.dto.response.empresa;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class EmpresaListItemDto {
  private Long id;
  private String codigoInterno;
  private String razonSocial;
  private String nit;
  private String rolPermitido;
  private String estado;
  private String erpUtilizado;
  private Boolean cobraInteres;
  private Boolean aplicaTasaEspecial;
  private Boolean tieneTasaPendiente;
}
