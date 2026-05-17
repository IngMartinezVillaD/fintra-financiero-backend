-- Agrega flag de centro de costo a cuentas PUC
ALTER TABLE prestamos.puc
  ADD COLUMN aplica_centro_costo BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN prestamos.puc.aplica_centro_costo
  IS 'Indica si la cuenta requiere centro de costo en la contabilización';
