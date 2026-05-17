package co.pluto.models.repositories;

import co.pluto.models.entity.AbonoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IAbonoRepository extends JpaRepository<AbonoEntity, Long> {

  List<AbonoEntity> findAllByOperacionIdOrderByFechaAsc(Long operacionId);

  Optional<AbonoEntity> findByOperacionIdAndNumeroComprobante(Long operacionId, String numeroComprobante);

  boolean existsByOperacionIdAndNumeroComprobante(Long operacionId, String numeroComprobante);

  @Query("SELECT COALESCE(SUM(a.aplicadoACapital), 0) FROM AbonoEntity a WHERE a.operacionId = :id")
  BigDecimal sumAplicadoACapital(@Param("id") Long operacionId);

  @Query("SELECT COALESCE(SUM(a.aplicadoAIntereses), 0) FROM AbonoEntity a WHERE a.operacionId = :id")
  BigDecimal sumAplicadoAIntereses(@Param("id") Long operacionId);
}
