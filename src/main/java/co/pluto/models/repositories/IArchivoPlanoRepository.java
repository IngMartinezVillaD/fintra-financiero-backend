package co.pluto.models.repositories;

import co.pluto.models.entity.ArchivoPlanoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IArchivoPlanoRepository extends JpaRepository<ArchivoPlanoEntity, Long> {

  List<ArchivoPlanoEntity> findAllByFechaGeneracionOrderByCreatedAtDesc(LocalDate fechaGeneracion);
}
