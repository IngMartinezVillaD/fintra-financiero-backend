package co.pluto.models.repositories;

import co.pluto.models.entity.LiquidacionMensualEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILiquidacionMensualRepository extends JpaRepository<LiquidacionMensualEntity, Long> {

  Optional<LiquidacionMensualEntity> findByAnioAndMes(Short anio, Short mes);

  boolean existsByAnioAndMes(Short anio, Short mes);

  List<LiquidacionMensualEntity> findAllByOrderByAnioDescMesDesc();
}
