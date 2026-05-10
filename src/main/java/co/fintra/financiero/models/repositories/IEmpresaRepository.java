package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.EmpresaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IEmpresaRepository extends JpaRepository<EmpresaEntity, Long> {

  boolean existsByCodigoInterno(String codigoInterno);
  boolean existsByNit(String nit);
  boolean existsByCodigoInternoAndIdNot(String codigoInterno, Long id);
  boolean existsByNitAndIdNot(String nit, Long id);

  @Query(
      value = """
          SELECT * FROM prestamos.empresas
          WHERE deleted_at IS NULL
            AND (:estado::varchar      IS NULL OR estado        = :estado)
            AND (:rolPermitido::varchar IS NULL OR rol_permitido = :rolPermitido)
            AND (:busqueda::varchar     IS NULL OR (
                 LOWER(razon_social)   LIKE LOWER('%' || :busqueda || '%') OR
                 LOWER(codigo_interno) LIKE LOWER('%' || :busqueda || '%') OR
                 LOWER(nit)            LIKE LOWER('%' || :busqueda || '%')
            ))
          ORDER BY razon_social ASC
          """,
      countQuery = """
          SELECT COUNT(*) FROM prestamos.empresas
          WHERE deleted_at IS NULL
            AND (:estado::varchar      IS NULL OR estado        = :estado)
            AND (:rolPermitido::varchar IS NULL OR rol_permitido = :rolPermitido)
            AND (:busqueda::varchar     IS NULL OR (
                 LOWER(razon_social)   LIKE LOWER('%' || :busqueda || '%') OR
                 LOWER(codigo_interno) LIKE LOWER('%' || :busqueda || '%') OR
                 LOWER(nit)            LIKE LOWER('%' || :busqueda || '%')
            ))
          """,
      nativeQuery = true)
  Page<EmpresaEntity> buscar(
      @Param("estado") String estado,
      @Param("rolPermitido") String rolPermitido,
      @Param("busqueda") String busqueda,
      Pageable pageable);

  Optional<EmpresaEntity> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT COUNT(e) > 0 FROM EmpresaEntity e WHERE e.id = :id AND e.deletedAt IS NULL")
  boolean existsByIdActiva(@Param("id") Long id);

  List<EmpresaEntity> findAllByDeletedAtIsNullAndEstadoOrderByRazonSocialAsc(String estado);
}
