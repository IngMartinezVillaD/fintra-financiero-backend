package co.pluto.models.repositories;

import co.pluto.models.entity.LiquidacionMensualDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ILiquidacionMensualDetalleRepository extends JpaRepository<LiquidacionMensualDetalleEntity, Long> {

  List<LiquidacionMensualDetalleEntity> findAllByLiquidacionId(Long liquidacionId);

  Optional<LiquidacionMensualDetalleEntity> findByLiquidacionIdAndOperacionId(Long liquidacionId, Long operacionId);

  boolean existsByLiquidacionIdAndOperacionId(Long liquidacionId, Long operacionId);

  void deleteAllByLiquidacionId(Long liquidacionId);

  boolean existsByLiquidacionIdAndSaldoInicialId(Long liquidacionId, Long saldoInicialId);

  Optional<LiquidacionMensualDetalleEntity> findByLiquidacionIdAndSaldoInicialId(Long liquidacionId, Long saldoInicialId);

  @Query("SELECT COALESCE(SUM(d.retencionFuenteAplicada), 0) FROM LiquidacionMensualDetalleEntity d WHERE d.liquidacionId = :id")
  BigDecimal sumRetencionFuente(@Param("id") Long liquidacionId);

  @Query("SELECT COALESCE(SUM(d.retencionIcaAplicada), 0) FROM LiquidacionMensualDetalleEntity d WHERE d.liquidacionId = :id")
  BigDecimal sumRetencionIca(@Param("id") Long liquidacionId);
}
