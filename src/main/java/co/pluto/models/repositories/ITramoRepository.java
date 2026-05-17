package co.pluto.models.repositories;

import co.pluto.models.entity.TramoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ITramoRepository extends JpaRepository<TramoEntity, Long> {

  Optional<TramoEntity> findFirstByOperacionIdAndEstadoAndDeletedAtIsNullOrderByNumeroTramoDesc(
      Long operacionId, String estado);

  List<TramoEntity> findAllByOperacionIdAndDeletedAtIsNullOrderByFechaDesdeAsc(Long operacionId);

  @Query("SELECT COALESCE(SUM(t.interesCalculado), 0) FROM TramoEntity t " +
         "WHERE t.operacion.id = :id AND t.estado = 'LIQUIDADO' AND t.deletedAt IS NULL")
  BigDecimal sumInteresLiquidado(@Param("id") Long operacionId);

  @Query("SELECT COALESCE(MAX(t.numeroTramo), 0) FROM TramoEntity t " +
         "WHERE t.operacion.id = :id AND t.deletedAt IS NULL")
  Integer maxNumeroTramo(@Param("id") Long operacionId);
}
