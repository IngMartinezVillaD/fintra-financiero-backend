package co.pluto.models.repositories;

import co.pluto.models.entity.AuditoriaLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuditoriaLoginRepository extends JpaRepository<AuditoriaLoginEntity, Long> {}
