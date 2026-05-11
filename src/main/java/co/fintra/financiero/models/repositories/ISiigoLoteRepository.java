package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.SiigoLoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISiigoLoteRepository extends JpaRepository<SiigoLoteEntity, Long> {

  Page<SiigoLoteEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
