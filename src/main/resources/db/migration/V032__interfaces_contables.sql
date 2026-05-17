-- ============================================================
-- V032 · Tipos de movimiento contable e Interfaces Contables
-- ============================================================

-- ------------------------------------------------------------
-- 1. Tipos de movimiento contable
-- ------------------------------------------------------------
CREATE TABLE prestamos.tipos_movimiento_contable (
  id          BIGSERIAL       PRIMARY KEY,
  codigo      VARCHAR(60)     NOT NULL UNIQUE,
  nombre      VARCHAR(200)    NOT NULL,
  descripcion TEXT,
  activo      BOOLEAN         NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE prestamos.tipos_movimiento_contable IS
  'Catálogo de tipos de movimiento contable usados en las interfaces.';

INSERT INTO prestamos.tipos_movimiento_contable (codigo, nombre, descripcion) VALUES
  ('DESEMBOLSO',           'Desembolso de préstamo',       'Registro contable del desembolso de dinero al prestatario'),
  ('CAUSACION_INTERESES',  'Causación de intereses',       'Registro de intereses causados en el período'),
  ('RECAUDO_INTERESES',    'Recaudo de intereses',         'Registro del recaudo efectivo de intereses'),
  ('ABONO_CAPITAL',        'Abono a capital',              'Registro de abono al capital del préstamo'),
  ('ABONO_INTERESES',      'Abono a intereses',            'Registro de abono a intereses causados'),
  ('LIQUIDACION_MENSUAL',  'Liquidación mensual',          'Asiento de liquidación mensual de intereses')
ON CONFLICT (codigo) DO NOTHING;

-- ------------------------------------------------------------
-- 2. Interfaces contables (cabecera)
-- ------------------------------------------------------------
CREATE TABLE prestamos.interfaces_contables (
  id                  BIGSERIAL       PRIMARY KEY,
  empresa_id          BIGINT          NOT NULL REFERENCES prestamos.empresas(id),
  tipo_movimiento_id  BIGINT          NOT NULL REFERENCES prestamos.tipos_movimiento_contable(id),
  nombre              VARCHAR(200)    NOT NULL,
  descripcion         TEXT,
  activa              BOOLEAN         NOT NULL DEFAULT TRUE,
  created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  created_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
  updated_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
  version             BIGINT          NOT NULL DEFAULT 0,
  deleted_at          TIMESTAMPTZ,
  UNIQUE (empresa_id, tipo_movimiento_id)
);

CREATE INDEX idx_interfaces_contables_empresa       ON prestamos.interfaces_contables(empresa_id);
CREATE INDEX idx_interfaces_contables_tipo          ON prestamos.interfaces_contables(tipo_movimiento_id);

COMMENT ON TABLE prestamos.interfaces_contables IS
  'Plantillas de interfaces contables por empresa y tipo de movimiento.';

-- ------------------------------------------------------------
-- 3. Líneas de cada interfaz contable
-- ------------------------------------------------------------
CREATE TABLE prestamos.interfaces_contables_lineas (
  id                  BIGSERIAL       PRIMARY KEY,
  interfaz_id         BIGINT          NOT NULL REFERENCES prestamos.interfaces_contables(id) ON DELETE CASCADE,
  orden               SMALLINT        NOT NULL,
  cuenta_puc_id       BIGINT          NOT NULL REFERENCES prestamos.puc(id),
  naturaleza          VARCHAR(10)     NOT NULL CHECK (naturaleza IN ('DEBITO','CREDITO')),
  descripcion_glosa   VARCHAR(300),
  created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  created_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
  updated_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
  version             BIGINT          NOT NULL DEFAULT 0,
  UNIQUE (interfaz_id, orden)
);

CREATE INDEX idx_interfaces_contables_lineas_interfaz ON prestamos.interfaces_contables_lineas(interfaz_id);

COMMENT ON TABLE prestamos.interfaces_contables_lineas IS
  'Líneas (débitos/créditos) de cada plantilla de interfaz contable.';
