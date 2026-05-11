package co.fintra.financiero.dto.response.operaciones;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder
public class OperacionResponseDto {
  private Long id;
  private String referencia;
  private Long empresaPrestamistaId;
  private String empresaPrestamistaNombre;
  private String empresaPrestamistaCodigoInterno;
  private Long empresaPrestatariaId;
  private String empresaPrestatariaNombre;
  private String empresaPrestatariaCodigoInterno;
  private String cobraInteres;
  private Long cuentaOrigenId;
  private String cuentaOrigenDescripcion;
  private Long cuentaDestinoId;
  private String cuentaDestinoDescripcion;
  private BigDecimal montoEstimado;
  private String observaciones;
  private String numDocumentoSoporte;
  private String estadoPipeline;
  private LocalDate fechaCreacion;
  private String creadoPor;
  private OffsetDateTime aprobacionInternaAt;
  private String aprobacionInternaUsuario;
  private String aprobacionInternaObservacion;
  private OffsetDateTime aceptacionEmpresaAt;
  private String aceptacionEmpresaUsuario;
  private String aceptacionEmpresaObservacion;
  private Boolean cuentaOrigenExentaGmf;
  private OffsetDateTime firmaDigitalAt;
  private OffsetDateTime desembolsoAt;
  private List<EventoPipelineDto> eventos;
  private AvisoTramoAnteriorDto avisoTramoAnterior;
}
