package co.fintra.financiero.services.impl.bancos;

import co.fintra.financiero.models.entity.DesembolsoEntity;
import co.fintra.financiero.models.entity.OperacionEntity;

import java.time.LocalDate;
import java.util.List;

public interface ArchivoPlanoGenerator {

  String bancoCodigo();

  String formato();

  ArchivoPlanoResult generar(List<OperacionEntity> operaciones,
                              List<DesembolsoEntity> desembolsos,
                              LocalDate fechaDesembolso);
}
