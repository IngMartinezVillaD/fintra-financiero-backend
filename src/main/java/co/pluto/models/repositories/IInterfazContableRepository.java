package co.pluto.models.repositories;

import co.pluto.models.entity.InterfazContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IInterfazContableRepository extends JpaRepository<InterfazContableEntity, Long> {

  List<InterfazContableEntity> findAllByDeletedAtIsNullOrderByEmpresaIdAscTipoMovimientoIdAsc();

  Optional<InterfazContableEntity> findByIdAndDeletedAtIsNull(Long id);

  Optional<InterfazContableEntity> findByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(Long empresaId, Long tipoMovimientoId);

  boolean existsByEmpresaIdAndTipoMovimientoIdAndDeletedAtIsNull(Long empresaId, Long tipoMovimientoId);

  List<InterfazContableEntity> findAllByEmpresaIdAndDeletedAtIsNull(Long empresaId);
}
