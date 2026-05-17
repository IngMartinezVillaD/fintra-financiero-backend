package co.pluto.models.repositories;

import co.pluto.models.entity.SaldoInicialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ISaldoInicialRepository extends JpaRepository<SaldoInicialEntity, Long> {

  List<SaldoInicialEntity> findAllByDeletedAtIsNullOrderByIdDesc();

  Optional<SaldoInicialEntity> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT s.codigo FROM SaldoInicialEntity s WHERE s.codigo LIKE 'SLD-%'")
  List<String> findAllCodigosSld();

  boolean existsByCodigo(String codigo);

  List<SaldoInicialEntity> findAllByEstadoAndDeletedAtIsNull(String estado);
}
