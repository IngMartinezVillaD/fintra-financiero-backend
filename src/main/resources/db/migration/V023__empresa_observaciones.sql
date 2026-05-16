-- ============================================================
-- V023 — Campo observaciones en empresas
-- ============================================================

ALTER TABLE prestamos.empresas
  ADD COLUMN IF NOT EXISTS observaciones VARCHAR(500);
