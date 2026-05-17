package co.pluto.models.repositories;

import co.pluto.models.entity.TasaPeriodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITasaPeriodoRepository extends JpaRepository<TasaPeriodoEntity, Long> {

  List<TasaPeriodoEntity> findAllByDeletedAtIsNullOrderByAnioDescMesDescTipoTasaAsc();

  Optional<TasaPeriodoEntity> findByAnioAndMesAndTipoTasaAndDeletedAtIsNull(Short anio, Short mes, String tipoTasa);

  @Query("""
      SELECT t FROM TasaPeriodoEntity t
      WHERE t.estado = 'APROBADA'
        AND t.anio = :anio
        AND t.mes  = :mes
        AND t.deletedAt IS NULL
      """)
  List<TasaPeriodoEntity> findAprobadosParaMes(@Param("anio") short anio, @Param("mes") short mes);

  @Query("""
      SELECT t FROM TasaPeriodoEntity t
      WHERE t.estado = 'APROBADA'
        AND t.vigenciaDesde <= :hoy
        AND t.vigenciaHasta >= :hoy
        AND t.deletedAt IS NULL
      """)
  List<TasaPeriodoEntity> findVigentesEnFecha(@Param("hoy") LocalDate hoy);

  @Query("""
      SELECT t FROM TasaPeriodoEntity t
      WHERE t.estado = 'PENDIENTE'
        AND t.deletedAt IS NULL
      ORDER BY t.anio DESC, t.mes DESC
      """)
  List<TasaPeriodoEntity> findPendientes();
}
