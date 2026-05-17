package co.pluto.services.impl.bancos;

import co.pluto.models.entity.DesembolsoEntity;
import co.pluto.models.entity.OperacionEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generador stub común: produce CSV mientras no llegue el spec técnico del banco.
 * Cada banco sobreescribe bancoCodigo() y formato(); el contenido se genera aquí.
 */
abstract class StubArchivoPlanoGenerator implements ArchivoPlanoGenerator {

  @Override
  public ArchivoPlanoResult generar(List<OperacionEntity> operaciones,
                                     List<DesembolsoEntity> desembolsos,
                                     LocalDate fechaDesembolso) {
    Map<Long, DesembolsoEntity> desembolsoByOp = desembolsos.stream()
        .collect(Collectors.toMap(DesembolsoEntity::getOperacionId, d -> d));

    StringBuilder sb = new StringBuilder();
    sb.append("BANCO;").append(bancoCodigo())
      .append(";FORMATO_PENDIENTE_DEFINICION;FECHA;").append(fechaDesembolso).append("\n");
    sb.append("REF;PRESTAMISTA;PRESTATARIA;CUENTA_ORIGEN;CUENTA_DESTINO;MONTO\n");

    BigDecimal total = BigDecimal.ZERO;
    for (OperacionEntity op : operaciones) {
      DesembolsoEntity d = desembolsoByOp.get(op.getId());
      BigDecimal monto = d != null ? d.getMonto() : op.getMontoEstimado();
      if (monto == null) monto = BigDecimal.ZERO;

      sb.append(op.getReferencia()).append(";")
        .append(op.getEmpresaPrestamista().getCodigoInterno()).append(";")
        .append(op.getEmpresaPrestataria().getCodigoInterno()).append(";")
        .append(op.getCuentaOrigen() != null ? op.getCuentaOrigen().getNumeroCuenta() : "").append(";")
        .append(op.getCuentaDestino() != null ? op.getCuentaDestino().getNumeroCuenta() : "").append(";")
        .append(monto).append("\n");

      total = total.add(monto);
    }

    return ArchivoPlanoResult.builder()
        .contenido(sb.toString())
        .totalRegistros(operaciones.size())
        .totalMonto(total)
        .build();
  }
}
