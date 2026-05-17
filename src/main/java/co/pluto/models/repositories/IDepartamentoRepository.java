package co.pluto.models.repositories;

import co.pluto.models.entity.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IDepartamentoRepository extends JpaRepository<DepartamentoEntity, Integer> {
  List<DepartamentoEntity> findAllByPaisCodigoIso2AndActivoTrueOrderByNombreAsc(String paisCodigo);
  List<DepartamentoEntity> findAllByPaisCodigoIso2OrderByNombreAsc(String paisCodigo);
  boolean existsByCodigoDaneAndPaisId(String codigoDane, Integer paisId);
  boolean existsByCodigoDaneAndPaisIdAndIdNot(String codigoDane, Integer paisId, Integer id);
}
