package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.PaisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPaisRepository extends JpaRepository<PaisEntity, Integer> {
  List<PaisEntity> findAllByActivoTrueOrderByNombreAsc();
  List<PaisEntity> findAllByOrderByNombreAsc();
  boolean existsByCodigoIso2(String codigoIso2);
  boolean existsByCodigoIso3(String codigoIso3);
  boolean existsByCodigoIso2AndIdNot(String codigoIso2, Integer id);
  boolean existsByCodigoIso3AndIdNot(String codigoIso3, Integer id);
}
