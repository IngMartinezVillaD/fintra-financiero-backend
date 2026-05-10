-- ============================================================
-- V009 — Tramos de interés
-- ============================================================

-- liquidacion_id referencia prestamos.liquidaciones_mensuales (V012)
-- FK se añade en V020 para evitar dependencia circular
CREATE TABLE prestamos.tramos (
  id               BIGSERIAL     PRIMARY KEY,
  operacion_id     BIGINT        NOT NULL,
  numero_tramo     INT           NOT NULL CHECK (numero_tramo > 0),
  tipo_movimiento  VARCHAR(50)   NOT NULL,
  fecha_desde      DATE          NOT NULL,
  fecha_hasta      DATE          NOT NULL,
  saldo_capital    NUMERIC(19,6) NOT NULL CHECK (saldo_capital >= 0),
  dias             INT           NOT NULL CHECK (dias > 0),
  tasa_porcentaje_mensual NUMERIC(8,4) NOT NULL CHECK (tasa_porcentaje_mensual >= 0),
  tipo_tasa        VARCHAR(20)   NOT NULL,
  interes_calculado NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (interes_calculado >= 0),
  estado           VARCHAR(20)   NOT NULL DEFAULT 'EN_CURSO',
  liquidacion_id   BIGINT,                                          -- FK -> liquidaciones_mensuales (V020)
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by       VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by       VARCHAR(100)  NOT NULL DEFAULT 'system',
  version          BIGINT        NOT NULL DEFAULT 0,
  deleted_at       TIMESTAMPTZ,

  CONSTRAINT uq_tramos_operacion_numero       UNIQUE (operacion_id, numero_tramo),
  CONSTRAINT chk_tramos_tipo_movimiento       CHECK (tipo_movimiento IN (
    'DESEMBOLSO_INICIAL','LIQUIDACION_CIERRE_MES','LIQUIDACION_PARCIAL_CAMBIO_TASA',
    'LIQUIDACION_NUEVO_DESEMBOLSO','LIQUIDACION_POR_ABONO'
  )),
  CONSTRAINT chk_tramos_tipo_tasa             CHECK (tipo_tasa IN ('COMERCIAL','ESPECIAL','SIN_INTERES')),
  CONSTRAINT chk_tramos_estado                CHECK (estado IN ('EN_CURSO','LIQUIDADO','ANULADO')),
  CONSTRAINT chk_tramos_fechas                CHECK (fecha_hasta >= fecha_desde),
  CONSTRAINT fk_tramos_operacion              FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id) ON DELETE RESTRICT
);

COMMENT ON COLUMN prestamos.tramos.liquidacion_id IS 'FK a prestamos.liquidaciones_mensuales — constraint añadido en V020';
