package co.fintra.financiero.services.impl.bancos;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value @Builder
public class ArchivoPlanoResult {
  String contenido;
  int totalRegistros;
  BigDecimal totalMonto;
}
