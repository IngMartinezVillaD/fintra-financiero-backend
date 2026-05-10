-- ============================================================
-- V016 — Tablas históricas de auditoría (*_history)
-- ============================================================

-- Función auxiliar: inserta en la tabla _history correspondiente
-- (los triggers concretos se crean en V017)

-- ── seguridad.usuarios_history ────────────────────────────────
CREATE TABLE auditoria.usuarios_history (
  history_id  BIGSERIAL    PRIMARY KEY,
  operation   CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by  VARCHAR(100),
  -- columnas espejo
  id              BIGINT,
  username        VARCHAR(100),
  nombre          VARCHAR(200),
  email           VARCHAR(255),
  activo          BOOLEAN,
  deleted_at      TIMESTAMPTZ,
  version         BIGINT
);

-- ── prestamos.empresas_history ────────────────────────────────
CREATE TABLE auditoria.empresas_history (
  history_id       BIGSERIAL    PRIMARY KEY,
  operation        CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by       VARCHAR(100),
  id               BIGINT,
  codigo_interno   VARCHAR(20),
  razon_social     VARCHAR(200),
  nit              VARCHAR(20),
  estado           VARCHAR(20),
  rol_permitido    VARCHAR(20),
  erp_utilizado    VARCHAR(20),
  cobra_interes    BOOLEAN,
  deleted_at       TIMESTAMPTZ,
  version          BIGINT
);

-- ── prestamos.tasas_periodo_history ──────────────────────────
CREATE TABLE auditoria.tasas_periodo_history (
  history_id                      BIGSERIAL   PRIMARY KEY,
  operation                       CHAR(1)     NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
  changed_by                      VARCHAR(100),
  id                              BIGINT,
  anio                            SMALLINT,
  mes                             SMALLINT,
  tipo_tasa                       VARCHAR(30),
  valor_porcentaje_efectivo_anual NUMERIC(8,4),
  valor_porcentaje_mensual        NUMERIC(8,4),
  estado                          VARCHAR(20),
  version                         BIGINT
);

-- ── prestamos.tasas_especiales_empresa_history ───────────────
CREATE TABLE auditoria.tasas_especiales_empresa_history (
  history_id                      BIGSERIAL   PRIMARY KEY,
  operation                       CHAR(1)     NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
  changed_by                      VARCHAR(100),
  id                              BIGINT,
  empresa_id                      BIGINT,
  valor_porcentaje_efectivo_anual NUMERIC(8,4),
  valor_porcentaje_mensual        NUMERIC(8,4),
  vigencia_desde                  DATE,
  vigencia_hasta                  DATE,
  estado                          VARCHAR(20),
  version                         BIGINT
);

-- ── prestamos.operaciones_history ────────────────────────────
CREATE TABLE auditoria.operaciones_history (
  history_id          BIGSERIAL   PRIMARY KEY,
  operation           CHAR(1)     NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  changed_by          VARCHAR(100),
  id                  BIGINT,
  referencia          VARCHAR(20),
  empresa_prestamista_id BIGINT,
  empresa_prestataria_id BIGINT,
  estado_pipeline     VARCHAR(20),
  cobra_interes       VARCHAR(20),
  deleted_at          TIMESTAMPTZ,
  version             BIGINT
);

-- ── prestamos.tramos_history ──────────────────────────────────
CREATE TABLE auditoria.tramos_history (
  history_id             BIGSERIAL    PRIMARY KEY,
  operation              CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by             VARCHAR(100),
  id                     BIGINT,
  operacion_id           BIGINT,
  numero_tramo           INT,
  saldo_capital          NUMERIC(19,6),
  interes_calculado      NUMERIC(19,6),
  estado                 VARCHAR(20),
  version                BIGINT
);

-- ── prestamos.desembolsos_history ─────────────────────────────
CREATE TABLE auditoria.desembolsos_history (
  history_id   BIGSERIAL    PRIMARY KEY,
  operation    CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by   VARCHAR(100),
  id           BIGINT,
  operacion_id BIGINT,
  monto        NUMERIC(19,6),
  fecha        DATE,
  gmf_calculado NUMERIC(19,6),
  version      BIGINT
);

-- ── prestamos.abonos_history ──────────────────────────────────
CREATE TABLE auditoria.abonos_history (
  history_id           BIGSERIAL    PRIMARY KEY,
  operation            CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by           VARCHAR(100),
  id                   BIGINT,
  operacion_id         BIGINT,
  monto_total          NUMERIC(19,6),
  aplicado_a_intereses NUMERIC(19,6),
  aplicado_a_capital   NUMERIC(19,6),
  fecha                DATE,
  version              BIGINT
);

-- ── prestamos.liquidaciones_mensuales_history ─────────────────
CREATE TABLE auditoria.liquidaciones_mensuales_history (
  history_id                 BIGSERIAL    PRIMARY KEY,
  operation                  CHAR(1)      NOT NULL CHECK (operation IN ('I','U','D')),
  changed_at                 TIMESTAMPTZ  NOT NULL DEFAULT now(),
  changed_by                 VARCHAR(100),
  id                         BIGINT,
  anio                       SMALLINT,
  mes                        SMALLINT,
  estado                     VARCHAR(30),
  total_intereses_liquidados NUMERIC(19,6),
  version                    BIGINT
);
