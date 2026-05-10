package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

  Optional<UsuarioEntity> findByUsernameAndDeletedAtIsNull(String username);

  Optional<UsuarioEntity> findByEmailAndDeletedAtIsNull(String email);
}
