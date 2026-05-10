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

  @Query("""
      SELECT e FROM EmpresaEntity e
      WHERE e.deletedAt IS NULL
        AND (:estado IS NULL OR e.estado = :estado)
        AND (:rolPermitido IS NULL OR e.rolPermitido = :rolPermitido)
        AND (:busqueda IS NULL OR (
             LOWER(e.razonSocial)   LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
             LOWER(e.codigoInterno) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
             LOWER(e.nit)           LIKE LOWER(CONCAT('%', :busqueda, '%'))
        ))
      """)
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
