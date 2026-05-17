package co.pluto.models.repositories;

import co.pluto.models.entity.AsientoContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IAsientoContableRepository
    extends JpaRepository<AsientoContableEntity, Long>,
            JpaSpecificationExecutor<AsientoContableEntity> {

  List<AsientoContableEntity> findAllByTipoOrigenAndOrigenIdOrderByIdAsc(String tipoOrigen, Long origenId);

  boolean existsByTipoOrigenAndOrigenIdAndEmpresaId(String tipoOrigen, Long origenId, Long empresaId);

  void deleteAllByTipoOrigenAndOrigenId(String tipoOrigen, Long origenId);
}
