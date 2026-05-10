-- ============================================================
-- V017 — Triggers de auditoría (updated_at + history tables)
-- ============================================================

-- ── Función genérica para tablas history ─────────────────────
-- Cada tabla transaccional tiene su propia función para mapear columnas explícitamente

-- seguridad.usuarios
CREATE OR REPLACE FUNCTION auditoria.trg_fn_usuarios_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  INSERT INTO auditoria.usuarios_history
    (operation, changed_by, id, username, nombre, email, activo, deleted_at, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.updated_by ELSE NEW.updated_by END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.id   ELSE NEW.id   END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.username ELSE NEW.username END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.nombre   ELSE NEW.nombre   END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.email    ELSE NEW.email    END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.activo   ELSE NEW.activo   END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.deleted_at ELSE NEW.deleted_at END,
    CASE TG_OP WHEN 'DELETE' THEN OLD.version  ELSE NEW.version  END
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_usuarios
  AFTER INSERT OR UPDATE OR DELETE ON seguridad.usuarios
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_usuarios_history();

-- prestamos.empresas
CREATE OR REPLACE FUNCTION auditoria.trg_fn_empresas_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.empresas_history
    (operation, changed_by, id, codigo_interno, razon_social, nit, estado,
     rol_permitido, erp_utilizado, cobra_interes, deleted_at, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.codigo_interno, r.razon_social, r.nit, r.estado,
    r.rol_permitido, r.erp_utilizado, r.cobra_interes, r.deleted_at, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_empresas
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.empresas
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_empresas_history();

-- prestamos.tasas_periodo
CREATE OR REPLACE FUNCTION auditoria.trg_fn_tasas_periodo_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.tasas_periodo_history
    (operation, changed_by, id, anio, mes, tipo_tasa,
     valor_porcentaje_efectivo_anual, valor_porcentaje_mensual, estado, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.anio, r.mes, r.tipo_tasa,
    r.valor_porcentaje_efectivo_anual, r.valor_porcentaje_mensual, r.estado, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_tasas_periodo
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.tasas_periodo
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_tasas_periodo_history();

-- prestamos.tasas_especiales_empresa
CREATE OR REPLACE FUNCTION auditoria.trg_fn_tasas_especiales_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.tasas_especiales_empresa_history
    (operation, changed_by, id, empresa_id,
     valor_porcentaje_efectivo_anual, valor_porcentaje_mensual,
     vigencia_desde, vigencia_hasta, estado, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.empresa_id,
    r.valor_porcentaje_efectivo_anual, r.valor_porcentaje_mensual,
    r.vigencia_desde, r.vigencia_hasta, r.estado, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_tasas_especiales
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.tasas_especiales_empresa
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_tasas_especiales_history();

-- prestamos.operaciones
CREATE OR REPLACE FUNCTION auditoria.trg_fn_operaciones_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.operaciones_history
    (operation, changed_by, id, referencia, empresa_prestamista_id,
     empresa_prestataria_id, estado_pipeline, cobra_interes, deleted_at, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.referencia, r.empresa_prestamista_id,
    r.empresa_prestataria_id, r.estado_pipeline, r.cobra_interes, r.deleted_at, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_operaciones
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.operaciones
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_operaciones_history();

-- prestamos.tramos
CREATE OR REPLACE FUNCTION auditoria.trg_fn_tramos_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.tramos_history
    (operation, changed_by, id, operacion_id, numero_tramo,
     saldo_capital, interes_calculado, estado, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.operacion_id, r.numero_tramo,
    r.saldo_capital, r.interes_calculado, r.estado, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_tramos
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.tramos
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_tramos_history();

-- prestamos.desembolsos
CREATE OR REPLACE FUNCTION auditoria.trg_fn_desembolsos_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.desembolsos_history
    (operation, changed_by, id, operacion_id, monto, fecha, gmf_calculado, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.operacion_id, r.monto, r.fecha, r.gmf_calculado, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_desembolsos
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.desembolsos
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_desembolsos_history();

-- prestamos.abonos
CREATE OR REPLACE FUNCTION auditoria.trg_fn_abonos_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.abonos_history
    (operation, changed_by, id, operacion_id, monto_total,
     aplicado_a_intereses, aplicado_a_capital, fecha, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.operacion_id, r.monto_total,
    r.aplicado_a_intereses, r.aplicado_a_capital, r.fecha, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_abonos
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.abonos
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_abonos_history();

-- prestamos.liquidaciones_mensuales
CREATE OR REPLACE FUNCTION auditoria.trg_fn_liquidaciones_history()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
  r := CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
  INSERT INTO auditoria.liquidaciones_mensuales_history
    (operation, changed_by, id, anio, mes, estado, total_intereses_liquidados, version)
  VALUES (
    CASE TG_OP WHEN 'INSERT' THEN 'I' WHEN 'UPDATE' THEN 'U' ELSE 'D' END,
    r.updated_by, r.id, r.anio, r.mes, r.estado, r.total_intereses_liquidados, r.version
  );
  RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_audit_liquidaciones
  AFTER INSERT OR UPDATE OR DELETE ON prestamos.liquidaciones_mensuales
  FOR EACH ROW EXECUTE FUNCTION auditoria.trg_fn_liquidaciones_history();

-- ── Triggers updated_at ───────────────────────────────────────
CREATE TRIGGER trg_updated_at_usuarios
  BEFORE UPDATE ON seguridad.usuarios
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_bancos
  BEFORE UPDATE ON prestamos.bancos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_cuentas_contables
  BEFORE UPDATE ON prestamos.cuentas_contables
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_empresas
  BEFORE UPDATE ON prestamos.empresas
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_empresa_cuentas
  BEFORE UPDATE ON prestamos.empresa_cuentas_bancarias
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_tasas_periodo
  BEFORE UPDATE ON prestamos.tasas_periodo
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_tasas_especiales
  BEFORE UPDATE ON prestamos.tasas_especiales_empresa
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_operaciones
  BEFORE UPDATE ON prestamos.operaciones
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_tramos
  BEFORE UPDATE ON prestamos.tramos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_desembolsos
  BEFORE UPDATE ON prestamos.desembolsos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_abonos
  BEFORE UPDATE ON prestamos.abonos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_liquidaciones
  BEFORE UPDATE ON prestamos.liquidaciones_mensuales
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_gmf
  BEFORE UPDATE ON prestamos.gmf_movimientos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_updated_at_interes_presunto
  BEFORE UPDATE ON prestamos.interes_presunto_movimientos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
