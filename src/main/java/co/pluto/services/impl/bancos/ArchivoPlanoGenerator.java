package co.pluto.services.impl.bancos;

import co.pluto.models.entity.DesembolsoEntity;
import co.pluto.models.entity.OperacionEntity;

import java.time.LocalDate;
import java.util.List;

public interface ArchivoPlanoGenerator {

  String bancoCodigo();

  String formato();

  ArchivoPlanoResult generar(List<OperacionEntity> operaciones,
                              List<DesembolsoEntity> desembolsos,
                              LocalDate fechaDesembolso);
}
