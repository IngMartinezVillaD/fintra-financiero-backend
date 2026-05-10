package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

  Optional<UsuarioEntity> findByUsernameAndDeletedAtIsNull(String username);
  Optional<UsuarioEntity> findByEmailAndDeletedAtIsNull(String email);

  @Query(value = """
      SELECT COUNT(*) > 0 FROM seguridad.usuarios_empresas
      WHERE usuario_id = :usuarioId AND empresa_id = :empresaId
      """, nativeQuery = true)
  boolean existsVinculoUsuarioEmpresa(
      @Param("usuarioId") Long usuarioId,
      @Param("empresaId") Long empresaId);

  @Query(value = """
      SELECT empresa_id FROM seguridad.usuarios_empresas
      WHERE usuario_id = :usuarioId
      """, nativeQuery = true)
  List<Long> findEmpresaIdsByUsuarioId(@Param("usuarioId") Long usuarioId);
}
