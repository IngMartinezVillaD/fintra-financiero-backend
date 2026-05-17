package co.pluto.models.repositories;

import co.pluto.models.entity.EmpresaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IEmpresaRepository extends JpaRepository<EmpresaEntity, Long>,
                                            JpaSpecificationExecutor<EmpresaEntity> {

  boolean existsByCodigoInterno(String codigoInterno);
  boolean existsByNit(String nit);
  boolean existsByCodigoInternoAndIdNot(String codigoInterno, Long id);
  boolean existsByNitAndIdNot(String nit, Long id);

  @Query("SELECT e.codigoInterno FROM EmpresaEntity e WHERE e.codigoInterno LIKE 'EMP-%'")
  List<String> findAllCodigosInternoEmp();

  Optional<EmpresaEntity> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT COUNT(e) > 0 FROM EmpresaEntity e WHERE e.id = :id AND e.deletedAt IS NULL")
  boolean existsByIdActiva(@Param("id") Long id);

  List<EmpresaEntity> findAllByDeletedAtIsNullAndEstadoOrderByRazonSocialAsc(String estado);
}
