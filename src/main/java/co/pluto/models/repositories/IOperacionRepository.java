package co.pluto.models.repositories;

import co.pluto.models.entity.OperacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IOperacionRepository extends JpaRepository<OperacionEntity, Long> {

  Optional<OperacionEntity> findByIdAndDeletedAtIsNull(Long id);

  @Query(value = """
      SELECT * FROM prestamos.operaciones
      WHERE deleted_at IS NULL
        AND (CAST(:estado AS varchar)       IS NULL OR estado_pipeline        = :estado)
        AND (CAST(:prestamistaId AS bigint) IS NULL OR empresa_prestamista_id = :prestamistaId)
        AND (CAST(:prestatariaId AS bigint) IS NULL OR empresa_prestataria_id = :prestatariaId)
        AND (CAST(:referencia AS varchar)   IS NULL OR LOWER(referencia) LIKE LOWER('%' || :referencia || '%'))
      ORDER BY created_at DESC
      """,
      countQuery = """
      SELECT COUNT(*) FROM prestamos.operaciones
      WHERE deleted_at IS NULL
        AND (CAST(:estado AS varchar)       IS NULL OR estado_pipeline        = :estado)
        AND (CAST(:prestamistaId AS bigint) IS NULL OR empresa_prestamista_id = :prestamistaId)
        AND (CAST(:prestatariaId AS bigint) IS NULL OR empresa_prestataria_id = :prestatariaId)
        AND (CAST(:referencia AS varchar)   IS NULL OR LOWER(referencia) LIKE LOWER('%' || :referencia || '%'))
      """,
      nativeQuery = true)
  Page<OperacionEntity> buscar(
      @Param("estado") String estado,
      @Param("prestamistaId") Long prestamistaId,
      @Param("prestatariaId") Long prestatariaId,
      @Param("referencia") String referencia,
      Pageable pageable);

  List<OperacionEntity> findAllByEmpresaPrestatariaIdAndEstadoPipelineAndDeletedAtIsNull(
      Long empresaPrestatariaId, String estadoPipeline);

  List<OperacionEntity> findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc(String estado);

  @Query(value = """
      SELECT * FROM prestamos.operaciones
      WHERE estado_pipeline = 'AE'
        AND deleted_at IS NULL
        AND empresa_prestataria_id IN (:empresaIds)
      ORDER BY created_at ASC
      """, nativeQuery = true)
  List<OperacionEntity> findPendientesAceptacion(@Param("empresaIds") List<Long> empresaIds);
}
