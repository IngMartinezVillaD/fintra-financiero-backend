package co.pluto.models.repositories;

import co.pluto.models.entity.ThomasSigneSolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IThomasSigneSolicitudRepository extends JpaRepository<ThomasSigneSolicitudEntity, UUID> {

  Optional<ThomasSigneSolicitudEntity> findByOperacionId(Long operacionId);
  boolean existsByIdempotencyKey(String idempotencyKey);
  Optional<ThomasSigneSolicitudEntity> findByIdempotencyKey(String idempotencyKey);
}
