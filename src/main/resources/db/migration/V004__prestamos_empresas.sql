-- ============================================================
-- V004 — Maestro de empresas
-- ============================================================

CREATE TABLE prestamos.empresas (
  id                           BIGSERIAL     PRIMARY KEY,
  codigo_interno               VARCHAR(20)   NOT NULL,
  razon_social                 VARCHAR(200)  NOT NULL,
  nit                          VARCHAR(20)   NOT NULL,
  pais                         VARCHAR(100)  NOT NULL DEFAULT 'Colombia',
  ciudad                       VARCHAR(100),
  rol_permitido                VARCHAR(20)   NOT NULL,
  estado                       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',
  representante_legal_nombre   VARCHAR(200),
  representante_legal_email    VARCHAR(255),
  representante_legal_telefono VARCHAR(30),
  erp_utilizado                VARCHAR(20),
  cuenta_cxc_id                BIGINT,
  cuenta_cxp_id                BIGINT,
  centro_utilidad              VARCHAR(50),
  saldo_inicial_capital        NUMERIC(19,6) NOT NULL DEFAULT 0,
  saldo_inicial_intereses      NUMERIC(19,6) NOT NULL DEFAULT 0,
  fecha_corte_saldo_inicial    DATE,
  cobra_interes                BOOLEAN       NOT NULL DEFAULT FALSE,
  calcula_interes_presunto     BOOLEAN       NOT NULL DEFAULT FALSE,
  aplica_tasa_especial         BOOLEAN       NOT NULL DEFAULT FALSE,
  retencion_fuente_porcentaje  NUMERIC(5,2),
  retencion_ica_porcentaje     NUMERIC(5,2),
  created_at                   TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                   TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                   VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                   VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                      BIGINT        NOT NULL DEFAULT 0,
  deleted_at                   TIMESTAMPTZ,

  CONSTRAINT uq_empresas_codigo_interno        UNIQUE (codigo_interno),
  CONSTRAINT uq_empresas_nit                   UNIQUE (nit),
  CONSTRAINT chk_empresas_rol_permitido        CHECK (rol_permitido IN ('PRESTAMISTA', 'PRESTATARIA', 'AMBOS')),
  CONSTRAINT chk_empresas_estado               CHECK (estado IN ('ACTIVA', 'INACTIVA')),
  CONSTRAINT chk_empresas_erp                  CHECK (erp_utilizado IN ('APOTHEOSYS', 'SIIGO')),
  CONSTRAINT chk_empresas_saldo_capital        CHECK (saldo_inicial_capital >= 0),
  CONSTRAINT chk_empresas_saldo_intereses      CHECK (saldo_inicial_intereses >= 0),
  CONSTRAINT chk_empresas_retencion_fuente     CHECK (retencion_fuente_porcentaje IS NULL OR retencion_fuente_porcentaje >= 0),
  CONSTRAINT chk_empresas_retencion_ica        CHECK (retencion_ica_porcentaje    IS NULL OR retencion_ica_porcentaje    >= 0),
  CONSTRAINT fk_empresas_cuenta_cxc            FOREIGN KEY (cuenta_cxc_id) REFERENCES prestamos.cuentas_contables(id),
  CONSTRAINT fk_empresas_cuenta_cxp            FOREIGN KEY (cuenta_cxp_id) REFERENCES prestamos.cuentas_contables(id)
);

COMMENT ON TABLE prestamos.empresas IS 'Maestro de empresas participantes — prestamista, prestataria o ambas';

-- Vincula usuario EMPRESA_RECEPTORA a sus empresas
CREATE TABLE seguridad.usuarios_empresas (
  usuario_id BIGINT NOT NULL,
  empresa_id BIGINT NOT NULL,
  PRIMARY KEY (usuario_id, empresa_id),
  CONSTRAINT fk_usuarios_empresas_usuario FOREIGN KEY (usuario_id) REFERENCES seguridad.usuarios(id)  ON DELETE CASCADE,
  CONSTRAINT fk_usuarios_empresas_empresa FOREIGN KEY (empresa_id) REFERENCES prestamos.empresas(id)  ON DELETE CASCADE
);
