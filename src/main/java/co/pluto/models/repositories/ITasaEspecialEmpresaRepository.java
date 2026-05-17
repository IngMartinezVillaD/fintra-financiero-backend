package co.pluto.models.repositories;

import co.pluto.models.entity.TasaEspecialEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ITasaEspecialEmpresaRepository extends JpaRepository<TasaEspecialEmpresaEntity, Long> {

  List<TasaEspecialEmpresaEntity> findAllByEmpresaIdAndDeletedAtIsNullOrderByVigenciaDesdeDesc(Long empresaId);

  Optional<TasaEspecialEmpresaEntity> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

  boolean existsByEmpresaIdAndEstadoAndDeletedAtIsNull(Long empresaId, String estado);

  Optional<TasaEspecialEmpresaEntity> findFirstByEmpresaIdAndEstadoAndDeletedAtIsNull(Long empresaId, String estado);
}
