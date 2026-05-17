package co.pluto.models.repositories;

import co.pluto.models.entity.Bitrix24NotificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IBitrix24NotificacionRepository extends JpaRepository<Bitrix24NotificacionEntity, Long> {

  List<Bitrix24NotificacionEntity> findAllByEstadoOrderByCreatedAtDesc(String estado);

  Page<Bitrix24NotificacionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @Query("SELECT COUNT(n) FROM Bitrix24NotificacionEntity n WHERE n.estado = 'ENVIADA' " +
         "AND n.createdAt >= (CURRENT_TIMESTAMP - 1 DAY)")
  long countEnviadasUltimas24h();

  @Query("SELECT COUNT(n) FROM Bitrix24NotificacionEntity n WHERE n.estado = 'ERROR' " +
         "AND n.createdAt >= (CURRENT_TIMESTAMP - 1 DAY)")
  long countErroresUltimas24h();
}
