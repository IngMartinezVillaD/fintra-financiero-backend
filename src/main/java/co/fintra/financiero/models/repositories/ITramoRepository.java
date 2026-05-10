package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.TramoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ITramoRepository extends JpaRepository<TramoEntity, Long> {

  Optional<TramoEntity> findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(
      Long operacionId, String estado);
}
