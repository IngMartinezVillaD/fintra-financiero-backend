package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.CuentaContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICuentaContableRepository extends JpaRepository<CuentaContableEntity, Long> {
  List<CuentaContableEntity> findAllByActivaTrueOrderByCodigoAsc();
}
