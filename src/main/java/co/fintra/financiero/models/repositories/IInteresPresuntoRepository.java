package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.InteresPresuntoMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface IInteresPresuntoRepository extends JpaRepository<InteresPresuntoMovimientoEntity, Long> {

  boolean existsByEmpresaIdAndOperacionIdAndAnioAndMes(Long empresaId, Long operacionId, Short anio, Short mes);

  List<InteresPresuntoMovimientoEntity> findAllByEmpresaIdAndAnioOrderByMesAsc(Long empresaId, Short anio);

  List<InteresPresuntoMovimientoEntity> findAllByAnioAndMes(Short anio, Short mes);

  @Query("SELECT COALESCE(SUM(p.montoCalculado), 0) FROM InteresPresuntoMovimientoEntity p " +
         "WHERE p.empresaId = :id AND p.anio = :anio")
  BigDecimal sumAnualByEmpresa(@Param("id") Long empresaId, @Param("anio") Short anio);
}
