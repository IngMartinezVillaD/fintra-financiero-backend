-- ============================================================
-- R__seed_catalogos — Datos semilla de catálogos (repetible)
-- Se re-ejecuta solo si cambia el checksum del archivo.
-- ============================================================

-- Roles (pueden ya existir del V002 — ON CONFLICT DO NOTHING)
INSERT INTO seguridad.roles (nombre) VALUES
  ('ADMIN'), ('TESORERIA'), ('APROBADOR'),
  ('EMPRESA_RECEPTORA'), ('CONTABILIDAD'), ('CONSULTA')
ON CONFLICT (nombre) DO NOTHING;

-- Bancos colombianos principales
INSERT INTO prestamos.bancos (codigo, nombre, formato_archivo_plano) VALUES
  ('001', 'Bancolombia',            'BANCOLOMBIA_ACH'),
  ('002', 'Banco de Bogotá',        'BOGOTA_ACH'),
  ('006', 'Banco Davivienda',       'DAVIVIENDA_ACH'),
  ('013', 'BBVA Colombia',          'BBVA_ACH'),
  ('023', 'Banco de Occidente',     'OCCIDENTE_ACH'),
  ('031', 'Banco Itaú Colombia',    'ITAU_ACH'),
  ('058', 'Scotiabank Colpatria',   'COLPATRIA_ACH'),
  ('007', 'Bancafé (Vía Bancolombia)', NULL),
  ('040', 'Banco Agrario',          NULL)
ON CONFLICT (codigo) DO NOTHING;

-- Cuentas contables placeholder (TODO: reemplazar con códigos definitivos de Contabilidad)
INSERT INTO prestamos.cuentas_contables (codigo, descripcion, tipo) VALUES
  ('TODO-CXC', 'Cuentas por Cobrar Intercompañía — código pendiente', 'ACTIVO'),
  ('TODO-CXP', 'Cuentas por Pagar Intercompañía — código pendiente',  'PASIVO')
ON CONFLICT (codigo) DO NOTHING;
