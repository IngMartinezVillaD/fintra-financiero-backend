package co.pluto.models.repositories;

import co.pluto.models.entity.BancoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IBancoRepository extends JpaRepository<BancoEntity, Long> {
  List<BancoEntity> findAllByActivoTrueOrderByNombreAsc();
  boolean existsByCodigo(String codigo);
}
