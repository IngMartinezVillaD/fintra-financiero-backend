-- V030 · Incluye saldos iniciales en la liquidación mensual

-- La columna operacion_id se vuelve nullable (un detalle es para operación O saldo inicial)
ALTER TABLE prestamos.liquidaciones_mensuales_detalle
  ALTER COLUMN operacion_id DROP NOT NULL;

ALTER TABLE prestamos.liquidaciones_mensuales_detalle
  ADD COLUMN saldo_inicial_id BIGINT
    REFERENCES prestamos.saldos_iniciales(id);

-- Constraint: exactamente uno de los dos debe ser no nulo
ALTER TABLE prestamos.liquidaciones_mensuales_detalle
  ADD CONSTRAINT chk_detalle_fuente
    CHECK (
      (operacion_id IS NOT NULL AND saldo_inicial_id IS NULL) OR
      (operacion_id IS NULL     AND saldo_inicial_id IS NOT NULL)
    );

CREATE INDEX idx_liq_detalle_saldo_inicial ON prestamos.liquidaciones_mensuales_detalle(saldo_inicial_id);

COMMENT ON COLUMN prestamos.liquidaciones_mensuales_detalle.saldo_inicial_id IS
  'Saldo inicial migrado incluido en esta liquidación. Excluyente con operacion_id.';
