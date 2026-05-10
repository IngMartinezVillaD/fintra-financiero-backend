-- ============================================================
-- V015 — Tablas de integraciones externas
-- ============================================================

-- Thomas Signe — firma digital
CREATE TABLE integraciones.thomas_signe_solicitudes (
  id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  operacion_id     BIGINT       NOT NULL,
  documento_url    TEXT         NOT NULL,
  destinatario_email VARCHAR(255) NOT NULL,
  estado           VARCHAR(20)  NOT NULL DEFAULT 'ENVIADA',
  enviado_at       TIMESTAMPTZ,
  firmado_at       TIMESTAMPTZ,
  webhook_payload  JSONB,
  idempotency_key  VARCHAR(100) NOT NULL,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by       VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_by       VARCHAR(100) NOT NULL DEFAULT 'system',
  version          BIGINT       NOT NULL DEFAULT 0,

  CONSTRAINT uq_thomas_idempotency_key  UNIQUE (idempotency_key),
  CONSTRAINT chk_thomas_estado          CHECK (estado IN ('ENVIADA','FIRMADA','RECHAZADA','EXPIRADA')),
  CONSTRAINT fk_thomas_operacion        FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id) ON DELETE RESTRICT
);

CREATE INDEX ix_thomas_operacion ON integraciones.thomas_signe_solicitudes (operacion_id);
COMMENT ON TABLE integraciones.thomas_signe_solicitudes IS 'Firma Thomas Signe libera desembolso automáticamente vía webhook. idempotency_key obligatorio.';

-- Bitrix24 — notificaciones CRM
CREATE TABLE integraciones.bitrix24_notificaciones (
  id            BIGSERIAL    PRIMARY KEY,
  evento_codigo VARCHAR(60)  NOT NULL,
  payload       JSONB        NOT NULL,
  estado        VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
  reintentos    SMALLINT     NOT NULL DEFAULT 0,
  ultimo_error  TEXT,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

  CONSTRAINT chk_bitrix24_estado CHECK (estado IN ('PENDIENTE','ENVIADA','ERROR'))
);

-- Archivos planos bancarios (ACH)
CREATE TABLE integraciones.archivos_planos_bancarios (
  id               BIGSERIAL     PRIMARY KEY,
  banco_codigo     VARCHAR(20)   NOT NULL,
  formato          VARCHAR(50)   NOT NULL,
  contenido        TEXT          NOT NULL,
  total_registros  INT           NOT NULL DEFAULT 0,
  total_monto      NUMERIC(19,6) NOT NULL DEFAULT 0,
  fecha_generacion DATE          NOT NULL DEFAULT CURRENT_DATE,
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by       VARCHAR(100)  NOT NULL DEFAULT 'system'
);

-- Lotes Apotheosys (ERP)
CREATE TABLE integraciones.apotheosys_lotes (
  id                  BIGSERIAL    PRIMARY KEY,
  fecha               DATE         NOT NULL,
  estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
  total_registros     INT          NOT NULL DEFAULT 0,
  payload             JSONB,
  archivo_url         TEXT,
  errores             TEXT,
  created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by          VARCHAR(100) NOT NULL DEFAULT 'system',

  CONSTRAINT chk_apotheosys_estado CHECK (estado IN ('PENDIENTE','PROCESANDO','ENVIADO','ERROR'))
);

-- Lotes SIIGO (ERP)
CREATE TABLE integraciones.siigo_lotes (
  id              BIGSERIAL    PRIMARY KEY,
  fecha           DATE         NOT NULL,
  estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
  total_registros INT          NOT NULL DEFAULT 0,
  payload         JSONB,
  archivo_url     TEXT,
  errores         TEXT,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by      VARCHAR(100) NOT NULL DEFAULT 'system',

  CONSTRAINT chk_siigo_estado CHECK (estado IN ('PENDIENTE','PROCESANDO','ENVIADO','ERROR'))
);
