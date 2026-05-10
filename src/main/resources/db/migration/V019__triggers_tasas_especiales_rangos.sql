-- ============================================================
-- V019 — Trigger de ajuste de vigencia en tasas especiales
-- ============================================================

-- Al insertar una nueva tasa especial con estado VIGENTE,
-- cierra automáticamente (vigencia_hasta = nueva_vigencia_desde - 1 día)
-- la tasa VIGENTE anterior de esa empresa.
CREATE OR REPLACE FUNCTION prestamos.fn_ajustar_vigencia_tasa_especial()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.estado = 'VIGENTE' THEN
    UPDATE prestamos.tasas_especiales_empresa
    SET
      vigencia_hasta = NEW.vigencia_desde - INTERVAL '1 day',
      estado         = 'VENCIDA',
      updated_at     = now(),
      updated_by     = NEW.created_by
    WHERE
      empresa_id = NEW.empresa_id
      AND estado = 'VIGENTE'
      AND id     <> NEW.id
      AND deleted_at IS NULL;
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_tasas_especiales_ajustar_vigencia
  AFTER INSERT OR UPDATE OF estado ON prestamos.tasas_especiales_empresa
  FOR EACH ROW
  WHEN (NEW.estado = 'VIGENTE')
  EXECUTE FUNCTION prestamos.fn_ajustar_vigencia_tasa_especial();
