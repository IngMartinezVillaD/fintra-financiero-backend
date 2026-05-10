-- ============================================================
-- V018 — Generación automática de referencia PREST-YYYY-NNN
-- ============================================================

-- Tabla de control de secuencia por año
CREATE TABLE prestamos.operaciones_secuencia_anual (
  anio       SMALLINT PRIMARY KEY,
  ultimo_num INT      NOT NULL DEFAULT 0
);

-- Función que genera la referencia antes del INSERT
CREATE OR REPLACE FUNCTION prestamos.fn_generar_referencia_operacion()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_anio    SMALLINT := EXTRACT(YEAR FROM now())::SMALLINT;
  v_num     INT;
BEGIN
  -- Incremento atómico del contador anual
  INSERT INTO prestamos.operaciones_secuencia_anual (anio, ultimo_num)
    VALUES (v_anio, 1)
  ON CONFLICT (anio) DO UPDATE
    SET ultimo_num = prestamos.operaciones_secuencia_anual.ultimo_num + 1
  RETURNING ultimo_num INTO v_num;

  NEW.referencia := 'PREST-' || v_anio || '-' || LPAD(v_num::TEXT, 3, '0');
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_operaciones_referencia
  BEFORE INSERT ON prestamos.operaciones
  FOR EACH ROW
  WHEN (NEW.referencia IS NULL)
  EXECUTE FUNCTION prestamos.fn_generar_referencia_operacion();
