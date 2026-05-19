-- ============================================================
-- reset_datos_prueba.sql
-- Vacía todos los datos transaccionales y reinicia secuencias.
--
-- CONSERVA:
--   - seguridad: roles, usuarios, usuario_roles
--   - prestamos: bancos, cuentas_contables
--   - prestamos: puc, tipos_movimiento_contable, interfaces_contables*
--   - catalogos: paises, departamentos, ciudades
--
-- BORRA:
--   - empresas, empresa_cuentas_bancarias, usuarios_empresas
--   - cupos_rotativos, saldos_iniciales
--   - operaciones, tramos, desembolsos, abonos, operaciones_soportes
--   - tasas_periodo, tasas_especiales_empresa, tasas_especiales_rangos (si existe)
--   - liquidaciones_mensuales + detalle
--   - gmf_movimientos, interes_presunto_movimientos, eventos_pipeline
--   - auditoria history, integraciones, auditoria_login
--
-- USO: ejecutar en psql o DBeaver — SOLO en entornos de desarrollo/QA.
-- Si hay una transacción abortada previa, ejecute ROLLBACK; primero.
-- ============================================================

-- ── 1. Integraciones externas ─────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    integraciones.thomas_signe_solicitudes,
    integraciones.bitrix24_notificaciones,
    integraciones.archivos_planos_bancarios,
    integraciones.apotheosys_lotes,
    integraciones.siigo_lotes
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'integraciones: alguna tabla no existe, se omite';
END $$;

-- ── 2. Auditoría histórica ────────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    auditoria.usuarios_history,
    auditoria.empresas_history,
    auditoria.tasas_periodo_history,
    auditoria.tasas_especiales_empresa_history,
    auditoria.operaciones_history,
    auditoria.tramos_history,
    auditoria.desembolsos_history,
    auditoria.abonos_history,
    auditoria.liquidaciones_mensuales_history
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'auditoria: alguna tabla no existe, se omite';
END $$;

-- ── 3. Liquidaciones ──────────────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    prestamos.liquidaciones_mensuales_detalle,
    prestamos.liquidaciones_mensuales
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'liquidaciones: alguna tabla no existe, se omite';
END $$;

-- ── 4. Operaciones y tramos ───────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    prestamos.tramos,
    prestamos.abonos,
    prestamos.gmf_movimientos,
    prestamos.interes_presunto_movimientos,
    prestamos.desembolsos,
    prestamos.operaciones_soportes,
    prestamos.eventos_pipeline,
    prestamos.operaciones_secuencia_anual,
    prestamos.operaciones
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'operaciones: alguna tabla no existe, se omite';
END $$;

-- ── 5. Cupos rotativos y saldos iniciales ─────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    prestamos.cupos_rotativos,
    prestamos.saldos_iniciales
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'cupos/saldos: alguna tabla no existe, se omite';
END $$;

-- ── 6. Tasas ──────────────────────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    prestamos.tasas_especiales_empresa,
    prestamos.tasas_periodo
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'tasas: alguna tabla no existe, se omite';
END $$;

-- ── 7. Empresas y cuentas bancarias ──────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE
    seguridad.usuarios_empresas,
    prestamos.empresa_cuentas_bancarias,
    prestamos.empresas
  RESTART IDENTITY CASCADE;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'empresas: alguna tabla no existe, se omite';
END $$;

-- ── 8. Auditoría de login ─────────────────────────────────────
DO $$ BEGIN
  TRUNCATE TABLE seguridad.auditoria_login RESTART IDENTITY;
EXCEPTION WHEN undefined_table THEN
  RAISE NOTICE 'auditoria_login: tabla no existe, se omite';
END $$;

-- ── 10. Verificación ──────────────────────────────────────────
SELECT 'seguridad.roles'                          AS tabla, COUNT(*) AS filas FROM seguridad.roles
UNION ALL SELECT 'seguridad.usuarios',                      COUNT(*) FROM seguridad.usuarios
UNION ALL SELECT 'seguridad.usuario_roles',                 COUNT(*) FROM seguridad.usuario_roles
UNION ALL SELECT 'prestamos.empresas',                      COUNT(*) FROM prestamos.empresas
UNION ALL SELECT 'prestamos.empresa_cuentas_bancarias',     COUNT(*) FROM prestamos.empresa_cuentas_bancarias
UNION ALL SELECT 'prestamos.bancos',                        COUNT(*) FROM prestamos.bancos
UNION ALL SELECT 'prestamos.cuentas_contables',             COUNT(*) FROM prestamos.cuentas_contables
UNION ALL SELECT 'prestamos.puc',                           COUNT(*) FROM prestamos.puc
UNION ALL SELECT 'prestamos.tipos_movimiento_contable',     COUNT(*) FROM prestamos.tipos_movimiento_contable
UNION ALL SELECT 'prestamos.interfaces_contables',          COUNT(*) FROM prestamos.interfaces_contables
UNION ALL SELECT 'prestamos.interfaces_contables_lineas',   COUNT(*) FROM prestamos.interfaces_contables_lineas
UNION ALL SELECT 'prestamos.cupos_rotativos',               COUNT(*) FROM prestamos.cupos_rotativos
UNION ALL SELECT 'prestamos.saldos_iniciales',              COUNT(*) FROM prestamos.saldos_iniciales
UNION ALL SELECT 'prestamos.tasas_periodo',                 COUNT(*) FROM prestamos.tasas_periodo
UNION ALL SELECT 'prestamos.operaciones',                   COUNT(*) FROM prestamos.operaciones
UNION ALL SELECT 'catalogos.ciudades',                      COUNT(*) FROM catalogos.ciudades
ORDER BY tabla;
