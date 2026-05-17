ALTER TABLE prestamos.operaciones
  ADD COLUMN fecha_vencimiento DATE         NULL,
  ADD COLUMN forma_pago        VARCHAR(10)  NULL CHECK (forma_pago IN ('BULLET', 'CUOTAS')),
  ADD COLUMN num_cuotas        SMALLINT     NULL CHECK (num_cuotas > 0);

COMMENT ON COLUMN prestamos.operaciones.fecha_vencimiento IS 'Fecha de vencimiento del préstamo';
COMMENT ON COLUMN prestamos.operaciones.forma_pago        IS 'BULLET = pago único al vencimiento, CUOTAS = amortización mensual';
COMMENT ON COLUMN prestamos.operaciones.num_cuotas        IS 'Número de cuotas (solo aplica cuando forma_pago = CUOTAS)';
