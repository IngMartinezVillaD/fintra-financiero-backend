package co.pluto.models.repositories;

import co.pluto.models.entity.CupoRotativoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ICupoRotativoRepository extends JpaRepository<CupoRotativoEntity, Long> {

  List<CupoRotativoEntity> findAllByDeletedAtIsNullOrderByIdDesc();

  Optional<CupoRotativoEntity> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT c.codigo FROM CupoRotativoEntity c WHERE c.codigo LIKE 'CUP-%'")
  List<String> findAllCodigosCup();

  boolean existsByCodigo(String codigo);

  List<CupoRotativoEntity> findAllByEmpresaIdAndEstadoAndDeletedAtIsNull(Long empresaId, String estado);
}
