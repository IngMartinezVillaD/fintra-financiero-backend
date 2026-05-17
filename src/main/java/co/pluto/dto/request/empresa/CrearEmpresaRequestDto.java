package co.pluto.dto.request.empresa;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CrearEmpresaRequestDto {

  @NotBlank @Size(max = 200)
  private String razonSocial;

  @NotBlank @Size(max = 20)
  private String nit;

  @Size(max = 100)
  private String pais = "Colombia";

  @Size(max = 100)
  private String ciudad;

  @Size(max = 100)
  private String departamento;

  @NotBlank
  @Pattern(regexp = "PRESTAMISTA|PRESTATARIA|AMBOS", message = "Debe ser PRESTAMISTA, PRESTATARIA o AMBOS")
  private String rolPermitido;

  @Size(max = 200)
  private String representanteLegalNombre;

  @Email @Size(max = 255)
  private String representanteLegalEmail;

  @Size(max = 30)
  private String representanteLegalTelefono;

  @Pattern(regexp = "APOTHEOSYS|SIIGO", message = "Debe ser APOTHEOSYS o SIIGO")
  private String erpUtilizado;

  private Long cuentaCxcId;
  private Long cuentaCxpId;

  @Size(max = 50)
  private String centroUtilidad;

  @DecimalMin("0") private BigDecimal saldoInicialCapital   = BigDecimal.ZERO;
  @DecimalMin("0") private BigDecimal saldoInicialIntereses = BigDecimal.ZERO;
  private LocalDate fechaCorteSaldoInicial;

  private Boolean cobraInteres            = false;
  private Boolean calculaInteresPresunto  = false;
  private Boolean aplicaTasaEspecial      = false;

  @DecimalMin("0") @DecimalMax("100")
  private BigDecimal retencionFuentePorcentaje;

  @DecimalMin("0") @DecimalMax("100")
  private BigDecimal retencionIcaPorcentaje;

  @Pattern(regexp = "ACTIVA|INACTIVA", message = "Debe ser ACTIVA o INACTIVA")
  private String estado = "ACTIVA";

  @Size(max = 500)
  private String observaciones;

  @Valid
  private List<CuentaBancariaRequestDto> cuentasBancarias;
}
