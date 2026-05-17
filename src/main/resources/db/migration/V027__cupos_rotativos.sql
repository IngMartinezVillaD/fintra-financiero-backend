-- ============================================================
-- V027 · Cupos Rotativos
-- ============================================================

CREATE TABLE prestamos.cupos_rotativos (
  id                       BIGSERIAL        PRIMARY KEY,
  codigo                   VARCHAR(20)      NOT NULL UNIQUE,
  empresa_id               BIGINT           NOT NULL
                             REFERENCES prestamos.empresas(id),
  tipo_tasa                VARCHAR(30)      NOT NULL
                             CHECK (tipo_tasa IN ('COMERCIAL_VIGENTE','PRESUNTA_FISCAL','ESPECIAL')),
  tasa_porcentaje_mensual  NUMERIC(8,4)     NOT NULL CHECK (tasa_porcentaje_mensual > 0),
  valor_cupo               NUMERIC(19,6)    NOT NULL CHECK (valor_cupo > 0),
  saldo_disponible         NUMERIC(19,6)    NOT NULL DEFAULT 0,
  estado                   VARCHAR(20)      NOT NULL DEFAULT 'ACTIVO'
                             CHECK (estado IN ('ACTIVO','SUSPENDIDO','CERRADO')),
  observaciones            TEXT,
  created_at               TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at               TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  created_by               VARCHAR(100)     NOT NULL DEFAULT 'system',
  updated_by               VARCHAR(100)     NOT NULL DEFAULT 'system',
  version                  BIGINT           NOT NULL DEFAULT 0,
  deleted_at               TIMESTAMPTZ
);

CREATE INDEX idx_cupos_rotativos_empresa  ON prestamos.cupos_rotativos(empresa_id);
CREATE INDEX idx_cupos_rotativos_estado   ON prestamos.cupos_rotativos(estado);

COMMENT ON TABLE prestamos.cupos_rotativos IS
  'Líneas de crédito rotativo preaprobadas por empresa. Código auto-generado CUP-NN.';
