package co.pluto.models.repositories;

import co.pluto.models.entity.CiudadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICiudadRepository extends JpaRepository<CiudadEntity, Integer> {
  List<CiudadEntity> findAllByDepartamentoCodigoDaneAndActivoTrueOrderByNombreAsc(String departamentoCodigo);
  List<CiudadEntity> findAllByDepartamentoCodigoDaneOrderByNombreAsc(String departamentoCodigo);
  boolean existsByCodigoDane(String codigoDane);
  boolean existsByCodigoDaneAndIdNot(String codigoDane, Integer id);
}
