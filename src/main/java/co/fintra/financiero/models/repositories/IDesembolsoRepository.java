package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.DesembolsoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IDesembolsoRepository extends JpaRepository<DesembolsoEntity, Long> {

  Optional<DesembolsoEntity> findByOperacionId(Long operacionId);

  boolean existsByOperacionId(Long operacionId);

  List<DesembolsoEntity> findAllByOperacionId(Long operacionId);
}
