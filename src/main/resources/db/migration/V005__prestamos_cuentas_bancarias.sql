-- ============================================================
-- V005 — Cuentas bancarias de empresas
-- ============================================================

CREATE TABLE prestamos.empresa_cuentas_bancarias (
  id                    BIGSERIAL    PRIMARY KEY,
  empresa_id            BIGINT       NOT NULL,
  banco_codigo          VARCHAR(20)  NOT NULL,
  tipo                  VARCHAR(20)  NOT NULL,
  numero_cuenta         VARCHAR(30)  NOT NULL,
  titular               VARCHAR(200) NOT NULL,
  codigo_contable       VARCHAR(30),
  formato_archivo_plano VARCHAR(50),
  exenta_gmf            BOOLEAN      NOT NULL DEFAULT FALSE,
  activa                BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by            VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_by            VARCHAR(100) NOT NULL DEFAULT 'system',
  version               BIGINT       NOT NULL DEFAULT 0,
  deleted_at            TIMESTAMPTZ,

  CONSTRAINT uq_empresa_cuenta_bancaria         UNIQUE (empresa_id, banco_codigo, numero_cuenta),
  CONSTRAINT chk_empresa_cuenta_tipo            CHECK (tipo IN ('CORRIENTE', 'AHORROS')),
  CONSTRAINT fk_empresa_cuentas_empresa         FOREIGN KEY (empresa_id)   REFERENCES prestamos.empresas(id) ON DELETE RESTRICT,
  CONSTRAINT fk_empresa_cuentas_banco           FOREIGN KEY (banco_codigo) REFERENCES prestamos.bancos(codigo)
);

COMMENT ON TABLE prestamos.empresa_cuentas_bancarias IS 'Cuentas bancarias registradas por empresa para desembolsos y ACH';
