package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.GmfMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IGmfMovimientoRepository extends JpaRepository<GmfMovimientoEntity, Long> {

  List<GmfMovimientoEntity> findAllByOperacionIdOrderByFechaAsc(Long operacionId);

  List<GmfMovimientoEntity> findAllByEmpresaIdAndAnioOrderByMesAsc(Long empresaId, Short anio);
}
