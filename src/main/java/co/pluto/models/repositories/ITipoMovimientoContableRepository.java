package co.pluto.models.repositories;

import co.pluto.models.entity.TipoMovimientoContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ITipoMovimientoContableRepository extends JpaRepository<TipoMovimientoContableEntity, Long> {

  List<TipoMovimientoContableEntity> findAllByActivoIsTrue();

  Optional<TipoMovimientoContableEntity> findByCodigoAndActivoIsTrue(String codigo);
}
