-- ============================================================
-- R__seed_dev_usuarios — Usuarios de prueba (solo dev/local)
-- Esta carpeta (db/dev-seed) solo la carga Flyway en perfil local.
-- NUNCA se ejecuta en producción.
-- ============================================================

-- Contraseña de todos: DevPass123! (BCrypt $2a$12$...)
INSERT INTO seguridad.usuarios (username, password, nombre, email, created_by, updated_by) VALUES
  ('tesoreria',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4vQj1l2A6m', 'Usuario Tesorería',          'tesoreria@pluto.co',   'system', 'system'),
  ('aprobador',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4vQj1l2A6m', 'Usuario Aprobador',          'aprobador@pluto.co',   'system', 'system'),
  ('empresa01',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4vQj1l2A6m', 'Usuario Empresa Receptora',  'empresa01@pluto.co',   'system', 'system'),
  ('contabilidad','$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4vQj1l2A6m', 'Usuario Contabilidad',       'contab@pluto.co',      'system', 'system'),
  ('consulta',    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4vQj1l2A6m', 'Usuario Solo Lectura',       'consulta@pluto.co',    'system', 'system')
ON CONFLICT (username) DO NOTHING;

-- Asignar roles
INSERT INTO seguridad.usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM seguridad.usuarios u CROSS JOIN seguridad.roles r
WHERE (u.username = 'tesoreria'    AND r.nombre = 'TESORERIA')
   OR (u.username = 'aprobador'    AND r.nombre = 'APROBADOR')
   OR (u.username = 'empresa01'    AND r.nombre = 'EMPRESA_RECEPTORA')
   OR (u.username = 'contabilidad' AND r.nombre = 'CONTABILIDAD')
   OR (u.username = 'consulta'     AND r.nombre = 'CONSULTA')
ON CONFLICT DO NOTHING;

-- Empresas de prueba para dev
INSERT INTO prestamos.empresas
  (codigo_interno, razon_social, nit, rol_permitido, estado, erp_utilizado,
   cobra_interes, calcula_interes_presunto, aplica_tasa_especial, created_by, updated_by)
VALUES
  ('EMP-01', 'Fintra S.A.S.',        '900123456-7', 'PRESTAMISTA',  'ACTIVA', 'APOTHEOSYS', TRUE,  TRUE,  TRUE,  'system', 'system'),
  ('EMP-02', 'Fintra Holding S.A.S.','800987654-3', 'PRESTATARIA',  'ACTIVA', 'SIIGO',      FALSE, FALSE, FALSE, 'system', 'system'),
  ('EMP-03', 'Fintra Inversiones',   '901555111-2', 'AMBOS',        'ACTIVA', 'APOTHEOSYS', TRUE,  FALSE, FALSE, 'system', 'system')
ON CONFLICT (codigo_interno) DO NOTHING;

-- Vincular empresa01 a EMP-02
INSERT INTO seguridad.usuarios_empresas (usuario_id, empresa_id)
SELECT u.id, e.id
FROM seguridad.usuarios u CROSS JOIN prestamos.empresas e
WHERE u.username = 'empresa01' AND e.codigo_interno = 'EMP-02'
ON CONFLICT DO NOTHING;
