-- ============================================================
-- V028 · Saldos Iniciales (migración de préstamos preexistentes)
-- ============================================================

CREATE TABLE prestamos.saldos_iniciales (
  id                       BIGSERIAL        PRIMARY KEY,
  codigo                   VARCHAR(20)      NOT NULL UNIQUE,
  empresa_prestamista_id   BIGINT           NOT NULL
                             REFERENCES prestamos.empresas(id),
  empresa_prestataria_id   BIGINT           NOT NULL
                             REFERENCES prestamos.empresas(id),
  CONSTRAINT chk_saldo_empresas_distintas
    CHECK (empresa_prestamista_id <> empresa_prestataria_id),
  tipo_tasa                VARCHAR(30)      NOT NULL
                             CHECK (tipo_tasa IN ('COMERCIAL_VIGENTE','PRESUNTA_FISCAL','ESPECIAL')),
  tasa_porcentaje_mensual  NUMERIC(8,4)     NOT NULL CHECK (tasa_porcentaje_mensual > 0),
  saldo_capital            NUMERIC(19,6)    NOT NULL CHECK (saldo_capital >= 0),
  intereses_acumulados     NUMERIC(19,6)    NOT NULL DEFAULT 0 CHECK (intereses_acumulados >= 0),
  fecha_corte              DATE             NOT NULL,
  estado                   VARCHAR(20)      NOT NULL DEFAULT 'ACTIVO'
                             CHECK (estado IN ('ACTIVO','MIGRADO','ANULADO')),
  observaciones            TEXT,
  created_at               TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at               TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  created_by               VARCHAR(100)     NOT NULL DEFAULT 'system',
  updated_by               VARCHAR(100)     NOT NULL DEFAULT 'system',
  version                  BIGINT           NOT NULL DEFAULT 0,
  deleted_at               TIMESTAMPTZ
);

CREATE INDEX idx_saldos_iniciales_prestamista  ON prestamos.saldos_iniciales(empresa_prestamista_id);
CREATE INDEX idx_saldos_iniciales_prestataria  ON prestamos.saldos_iniciales(empresa_prestataria_id);
CREATE INDEX idx_saldos_iniciales_estado       ON prestamos.saldos_iniciales(estado);

COMMENT ON TABLE prestamos.saldos_iniciales IS
  'Saldos de préstamos preexistentes al sistema. Código auto-generado SLD-NN.';
