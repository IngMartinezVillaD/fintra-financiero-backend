package co.pluto.dto.response.empresa;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder
public class EmpresaResponseDto {
  private Long id;
  private String codigoInterno;
  private String razonSocial;
  private String nit;
  private String pais;
  private String ciudad;
  private String departamento;
  private String rolPermitido;
  private String estado;
  private String representanteLegalNombre;
  private String representanteLegalEmail;
  private String representanteLegalTelefono;
  private String erpUtilizado;
  private Long cuentaCxcId;
  private String cuentaCxcCodigo;
  private Long cuentaCxpId;
  private String cuentaCxpCodigo;
  private String centroUtilidad;
  private BigDecimal saldoInicialCapital;
  private BigDecimal saldoInicialIntereses;
  private LocalDate fechaCorteSaldoInicial;
  private Boolean cobraInteres;
  private Boolean calculaInteresPresunto;
  private Boolean aplicaTasaEspecial;
  private BigDecimal retencionFuentePorcentaje;
  private BigDecimal retencionIcaPorcentaje;
  private String observaciones;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private List<CuentaBancariaResponseDto> cuentasBancarias;
  private List<TasaEspecialResponseDto> tasasEspeciales;
}
