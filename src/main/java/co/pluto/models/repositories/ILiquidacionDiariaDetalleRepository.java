package co.pluto.models.repositories;

import co.pluto.models.entity.LiquidacionDiariaDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ILiquidacionDiariaDetalleRepository extends JpaRepository<LiquidacionDiariaDetalleEntity, Long> {

  List<LiquidacionDiariaDetalleEntity> findAllByLiquidacionId(Long liquidacionId);

  boolean existsByLiquidacionIdAndOperacionId(Long liquidacionId, Long operacionId);

  boolean existsByLiquidacionIdAndSaldoInicialId(Long liquidacionId, Long saldoInicialId);

  void deleteAllByLiquidacionId(Long liquidacionId);

  @Query("SELECT COALESCE(SUM(d.retencionFuenteAplicada), 0) FROM LiquidacionDiariaDetalleEntity d WHERE d.liquidacionId = :id")
  BigDecimal sumRetencionFuente(@Param("id") Long liquidacionId);

  @Query("SELECT COALESCE(SUM(d.retencionIcaAplicada), 0) FROM LiquidacionDiariaDetalleEntity d WHERE d.liquidacionId = :id")
  BigDecimal sumRetencionIca(@Param("id") Long liquidacionId);
}
