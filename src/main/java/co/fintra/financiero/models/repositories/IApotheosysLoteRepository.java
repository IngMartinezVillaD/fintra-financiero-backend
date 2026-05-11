package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.ApotheosysLoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IApotheosysLoteRepository extends JpaRepository<ApotheosysLoteEntity, Long> {

  Page<ApotheosysLoteEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
