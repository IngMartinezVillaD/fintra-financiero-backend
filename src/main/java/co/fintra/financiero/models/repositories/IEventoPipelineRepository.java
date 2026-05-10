package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.EventoPipelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IEventoPipelineRepository extends JpaRepository<EventoPipelineEntity, Long> {
  List<EventoPipelineEntity> findAllByOperacionIdOrderByOcurridoAtAsc(Long operacionId);
}
