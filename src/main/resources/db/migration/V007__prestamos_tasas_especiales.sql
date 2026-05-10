-- ============================================================
-- V007 — Tasas especiales por empresa
-- ============================================================

CREATE TABLE prestamos.tasas_especiales_empresa (
  id                                  BIGSERIAL     PRIMARY KEY,
  empresa_id                          BIGINT        NOT NULL,
  valor_porcentaje_efectivo_anual     NUMERIC(8,4)  NOT NULL CHECK (valor_porcentaje_efectivo_anual > 0),
  valor_porcentaje_mensual            NUMERIC(8,4)  NOT NULL CHECK (valor_porcentaje_mensual > 0),
  vigencia_desde                      DATE          NOT NULL,
  vigencia_hasta                      DATE          NOT NULL,
  estado                              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
  aprobado_por_usuario_id             BIGINT,
  aprobado_at                         TIMESTAMPTZ,
  observacion                         TEXT,
  created_at                          TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                          TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                          VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                          VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                             BIGINT        NOT NULL DEFAULT 0,
  deleted_at                          TIMESTAMPTZ,

  CONSTRAINT chk_tasas_especiales_estado     CHECK (estado IN ('PENDIENTE', 'APROBADA', 'VIGENTE', 'VENCIDA', 'RECHAZADA')),
  CONSTRAINT chk_tasas_especiales_vigencia   CHECK (vigencia_hasta >= vigencia_desde),
  CONSTRAINT fk_tasas_especiales_empresa     FOREIGN KEY (empresa_id)              REFERENCES prestamos.empresas(id) ON DELETE RESTRICT,
  CONSTRAINT fk_tasas_especiales_aprobador   FOREIGN KEY (aprobado_por_usuario_id) REFERENCES seguridad.usuarios(id)
);

-- Solo puede haber una tasa VIGENTE por empresa a la vez
CREATE UNIQUE INDEX uq_tasas_especiales_una_vigente
  ON prestamos.tasas_especiales_empresa (empresa_id)
  WHERE estado = 'VIGENTE' AND deleted_at IS NULL;

COMMENT ON TABLE prestamos.tasas_especiales_empresa IS 'Tasa especial por empresa. Tasa especial vencida bloquea solo esa empresa. Máximo una VIGENTE por empresa.';
