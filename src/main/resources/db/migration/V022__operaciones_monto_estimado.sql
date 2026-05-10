-- ============================================================
-- V022 — Agrega monto_estimado a operaciones (campo informativo en etapa CR)
-- El monto real se registra en prestamos.desembolsos (etapa DS).
-- ============================================================

ALTER TABLE prestamos.operaciones
  ADD COLUMN monto_estimado NUMERIC(19,6) CHECK (monto_estimado > 0);

COMMENT ON COLUMN prestamos.operaciones.monto_estimado IS 'Monto referencial en CR. El monto efectivo queda en prestamos.desembolsos al completar DS.';
