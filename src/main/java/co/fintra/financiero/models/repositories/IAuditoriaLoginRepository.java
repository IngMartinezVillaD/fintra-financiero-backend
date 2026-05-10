package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.AuditoriaLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuditoriaLoginRepository extends JpaRepository<AuditoriaLoginEntity, Long> {}
