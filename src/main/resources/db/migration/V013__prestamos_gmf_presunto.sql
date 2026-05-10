-- ============================================================
-- V013 — GMF e interés presunto (extracontables)
-- ============================================================

-- GMF e interés presunto son SIEMPRE extracontables (nunca en contabilidad oficial)
CREATE TABLE prestamos.gmf_movimientos (
  id              BIGSERIAL     PRIMARY KEY,
  empresa_id      BIGINT        NOT NULL,
  operacion_id    BIGINT        NOT NULL,
  anio            SMALLINT      NOT NULL,
  mes             SMALLINT      NOT NULL CHECK (mes BETWEEN 1 AND 12),
  monto_gmf       NUMERIC(19,6) NOT NULL CHECK (monto_gmf >= 0),
  fecha           DATE          NOT NULL,
  decision_anual  VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by      VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by      VARCHAR(100)  NOT NULL DEFAULT 'system',
  version         BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT chk_gmf_decision            CHECK (decision_anual IN ('PENDIENTE','COBRAR','ASUMIR')),
  CONSTRAINT fk_gmf_empresa              FOREIGN KEY (empresa_id)   REFERENCES prestamos.empresas(id),
  CONSTRAINT fk_gmf_operacion            FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id)
);

CREATE TABLE prestamos.interes_presunto_movimientos (
  id                      BIGSERIAL     PRIMARY KEY,
  empresa_id              BIGINT        NOT NULL,
  operacion_id            BIGINT        NOT NULL,
  anio                    SMALLINT      NOT NULL,
  mes                     SMALLINT      NOT NULL CHECK (mes BETWEEN 1 AND 12),
  saldo_capital_promedio  NUMERIC(19,6) NOT NULL CHECK (saldo_capital_promedio >= 0),
  tasa_presunta_porcentaje NUMERIC(8,4) NOT NULL CHECK (tasa_presunta_porcentaje > 0),
  dias                    INT           NOT NULL CHECK (dias > 0),
  monto_calculado         NUMERIC(19,6) NOT NULL CHECK (monto_calculado >= 0),
  created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by              VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by              VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                 BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT fk_interes_presunto_empresa   FOREIGN KEY (empresa_id)   REFERENCES prestamos.empresas(id),
  CONSTRAINT fk_interes_presunto_operacion FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id)
);

COMMENT ON TABLE prestamos.gmf_movimientos               IS 'GMF (4x1000) — siempre extracontable, nunca en asiento oficial';
COMMENT ON TABLE prestamos.interes_presunto_movimientos  IS 'Interés presunto fiscal — siempre extracontable';
