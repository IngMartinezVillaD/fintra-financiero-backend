package co.pluto.models.repositories;

import co.pluto.models.entity.LiquidacionDiariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ILiquidacionDiariaRepository extends JpaRepository<LiquidacionDiariaEntity, Long> {

  List<LiquidacionDiariaEntity> findAllByOrderByFechaDesc();

  boolean existsByFecha(LocalDate fecha);

  Optional<LiquidacionDiariaEntity> findByFecha(LocalDate fecha);
}
