-- ============================================================
-- V012 — Liquidaciones mensuales
-- ============================================================

CREATE TABLE prestamos.liquidaciones_mensuales (
  id                          BIGSERIAL     PRIMARY KEY,
  anio                        SMALLINT      NOT NULL,
  mes                         SMALLINT      NOT NULL CHECK (mes BETWEEN 1 AND 12),
  fecha_corte                 DATE          NOT NULL,
  estado                      VARCHAR(30)   NOT NULL DEFAULT 'BORRADOR',
  aprobada_por                BIGINT,
  aprobada_at                 TIMESTAMPTZ,
  total_intereses_liquidados  NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (total_intereses_liquidados >= 0),
  created_at                  TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                  TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                  VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                  VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                     BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT uq_liquidaciones_anio_mes  UNIQUE (anio, mes),
  CONSTRAINT chk_liquidaciones_estado   CHECK (estado IN ('BORRADOR','PENDIENTE_APROBACION','APROBADA','CONTABILIZADA')),
  CONSTRAINT fk_liquidaciones_aprobador FOREIGN KEY (aprobada_por) REFERENCES seguridad.usuarios(id)
);

CREATE TABLE prestamos.liquidaciones_mensuales_detalle (
  id                       BIGSERIAL     PRIMARY KEY,
  liquidacion_id           BIGINT        NOT NULL,
  operacion_id             BIGINT        NOT NULL,
  intereses_periodo        NUMERIC(19,6) NOT NULL CHECK (intereses_periodo >= 0),
  retencion_fuente_aplicada NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (retencion_fuente_aplicada >= 0),
  retencion_ica_aplicada   NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (retencion_ica_aplicada >= 0),

  CONSTRAINT fk_liq_detalle_liquidacion FOREIGN KEY (liquidacion_id) REFERENCES prestamos.liquidaciones_mensuales(id) ON DELETE CASCADE,
  CONSTRAINT fk_liq_detalle_operacion   FOREIGN KEY (operacion_id)   REFERENCES prestamos.operaciones(id)
);

-- Ahora que liquidaciones_mensuales existe, FK pendiente en tramos se añade en V020
COMMENT ON TABLE prestamos.liquidaciones_mensuales IS 'Capital e intereses causados se llevan siempre separados (Campo 27 auto-generado, no editable).';
