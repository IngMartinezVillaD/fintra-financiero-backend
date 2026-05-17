package co.pluto.models.repositories;

import co.pluto.models.entity.InterfazContableLineaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IInterfazContableLineaRepository extends JpaRepository<InterfazContableLineaEntity, Long> {

  List<InterfazContableLineaEntity> findAllByInterfazIdOrderByOrdenAsc(Long interfazId);

  @Transactional
  void deleteAllByInterfazId(Long interfazId);
}
