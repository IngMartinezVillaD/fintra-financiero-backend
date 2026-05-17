-- Liquidaciones diarias: causación de 1 día de interés sin cierre/apertura de tramos

CREATE TABLE prestamos.liquidaciones_diarias (
  id                         BIGSERIAL     PRIMARY KEY,
  fecha                      DATE          NOT NULL,
  estado                     VARCHAR(30)   NOT NULL DEFAULT 'BORRADOR'
    CHECK (estado IN ('BORRADOR','PENDIENTE_APROBACION','APROBADA','CONTABILIZADA')),
  total_intereses_liquidados NUMERIC(19,6) NOT NULL DEFAULT 0,
  aprobada_por               BIGINT,
  aprobada_at                TIMESTAMPTZ,
  created_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                 VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                 VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                    BIGINT        NOT NULL DEFAULT 0,
  CONSTRAINT uq_liq_diaria_fecha UNIQUE (fecha)
);

CREATE TABLE prestamos.liquidaciones_diarias_detalle (
  id                        BIGSERIAL     PRIMARY KEY,
  liquidacion_id            BIGINT        NOT NULL REFERENCES prestamos.liquidaciones_diarias(id) ON DELETE CASCADE,
  operacion_id              BIGINT        REFERENCES prestamos.operaciones(id),
  saldo_inicial_id          BIGINT        REFERENCES prestamos.saldos_iniciales(id),
  intereses_periodo         NUMERIC(19,6) NOT NULL DEFAULT 0,
  retencion_fuente_aplicada NUMERIC(19,6) NOT NULL DEFAULT 0,
  retencion_ica_aplicada    NUMERIC(19,6) NOT NULL DEFAULT 0
);

CREATE INDEX idx_liq_diaria_detalle_liq ON prestamos.liquidaciones_diarias_detalle (liquidacion_id);

-- Ampliar el CHECK de tipo_origen en asientos_contables para admitir LIQUIDACION_DIARIA
ALTER TABLE prestamos.asientos_contables
  DROP CONSTRAINT IF EXISTS asientos_contables_tipo_origen_check;

ALTER TABLE prestamos.asientos_contables
  ADD CONSTRAINT asientos_contables_tipo_origen_check
  CHECK (tipo_origen IN ('LIQUIDACION','DESEMBOLSO','LIQUIDACION_DIARIA'));

COMMENT ON TABLE prestamos.liquidaciones_diarias         IS 'Causación diaria de intereses sin cierre de tramos';
COMMENT ON TABLE prestamos.liquidaciones_diarias_detalle IS 'Línea por operación/saldo inicial de cada liquidación diaria';
