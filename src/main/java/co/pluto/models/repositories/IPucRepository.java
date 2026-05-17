package co.pluto.models.repositories;

import co.pluto.models.entity.PucEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPucRepository extends JpaRepository<PucEntity, Long> {

  List<PucEntity> findAllByDeletedAtIsNullOrderByCodigoAsc();

  Optional<PucEntity> findByCodigoAndDeletedAtIsNull(String codigo);

  Optional<PucEntity> findByIdAndDeletedAtIsNull(Long id);

  List<PucEntity> findAllByCodigoStartingWithAndActivaIsTrueAndDeletedAtIsNull(String prefijo);

  List<PucEntity> findAllByCodigoContainingOrNombreContainingIgnoreCaseAndDeletedAtIsNull(String codigo, String nombre);

  List<PucEntity> findAllByNivelAndActivaIsTrueAndDeletedAtIsNullOrderByCodigoAsc(Short nivel);
}
