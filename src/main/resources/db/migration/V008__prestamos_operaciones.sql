-- ============================================================
-- V008 — Documentos, operaciones y soportes
-- ============================================================

-- Tabla genérica de adjuntos
CREATE TABLE prestamos.documentos (
  id            BIGSERIAL    PRIMARY KEY,
  nombre        VARCHAR(255) NOT NULL,
  content_type  VARCHAR(100) NOT NULL,
  tamano_bytes  BIGINT       NOT NULL CHECK (tamano_bytes > 0),
  url           TEXT         NOT NULL,
  checksum      VARCHAR(64),
  subido_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  subido_por    VARCHAR(100) NOT NULL
);

-- Pipeline de operaciones de préstamo intercompañía
-- firma_digital_documento_id y desembolso_archivo_plano_id son BIGINT sin FK
-- (referencias a integraciones.* que se crean en V015; FKs se añaden en V020)
CREATE TABLE prestamos.operaciones (
  id                               BIGSERIAL     PRIMARY KEY,
  referencia                       VARCHAR(20)   UNIQUE,            -- PREST-YYYY-NNN, generada por trigger V018
  empresa_prestamista_id           BIGINT        NOT NULL,
  empresa_prestataria_id           BIGINT        NOT NULL,
  cobra_interes                    VARCHAR(20)   NOT NULL DEFAULT 'NO',
  cuenta_origen_id                 BIGINT,
  cuenta_destino_id                BIGINT,
  observaciones                    TEXT          NOT NULL,          -- Campo 27 ERP, auto-generado en liquidación
  num_documento_soporte            VARCHAR(60)   NOT NULL,          -- Campo 10 ERP
  estado_pipeline                  VARCHAR(20)   NOT NULL DEFAULT 'CR',
  fecha_creacion                   DATE          NOT NULL DEFAULT CURRENT_DATE,
  -- Aprobación interna
  aprobacion_interna_at            TIMESTAMPTZ,
  aprobacion_interna_usuario_id    BIGINT,
  aprobacion_interna_observacion   TEXT,
  -- Aceptación empresa receptora
  aceptacion_empresa_at            TIMESTAMPTZ,
  aceptacion_empresa_usuario_id    BIGINT,
  aceptacion_empresa_observacion   TEXT,
  -- Firma digital (Thomas Signe)
  firma_digital_at                 TIMESTAMPTZ,
  firma_digital_documento_id       UUID,                            -- FK -> integraciones.thomas_signe_solicitudes.id (UUID, V020)
  -- Desembolso
  desembolso_at                    TIMESTAMPTZ,
  desembolso_archivo_plano_id      BIGINT,                          -- FK -> integraciones.archivos_planos_bancarios (V020)
  -- Auditoría
  created_at                       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                       VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                       VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                          BIGINT        NOT NULL DEFAULT 0,
  deleted_at                       TIMESTAMPTZ,

  CONSTRAINT chk_operaciones_cobra_interes    CHECK (cobra_interes IN ('SI_COMERCIAL', 'SI_ESPECIAL', 'NO')),
  CONSTRAINT chk_operaciones_estado           CHECK (estado_pipeline IN ('CR','AI','AE','FD','DS','RECHAZADA','CANCELADA')),
  CONSTRAINT chk_operaciones_empresas         CHECK (empresa_prestamista_id <> empresa_prestataria_id),
  CONSTRAINT fk_operaciones_prestamista       FOREIGN KEY (empresa_prestamista_id)        REFERENCES prestamos.empresas(id),
  CONSTRAINT fk_operaciones_prestataria       FOREIGN KEY (empresa_prestataria_id)        REFERENCES prestamos.empresas(id),
  CONSTRAINT fk_operaciones_cuenta_origen     FOREIGN KEY (cuenta_origen_id)              REFERENCES prestamos.empresa_cuentas_bancarias(id),
  CONSTRAINT fk_operaciones_cuenta_destino    FOREIGN KEY (cuenta_destino_id)             REFERENCES prestamos.empresa_cuentas_bancarias(id),
  CONSTRAINT fk_operaciones_aprobador_interno FOREIGN KEY (aprobacion_interna_usuario_id) REFERENCES seguridad.usuarios(id),
  CONSTRAINT fk_operaciones_aceptador         FOREIGN KEY (aceptacion_empresa_usuario_id) REFERENCES seguridad.usuarios(id)
);

COMMENT ON COLUMN prestamos.operaciones.observaciones             IS 'Campo 27 Apotheosys/SIIGO — auto-generado en liquidación, no editable';
COMMENT ON COLUMN prestamos.operaciones.firma_digital_documento_id IS 'FK a integraciones.thomas_signe_solicitudes — constraint añadido en V020';
COMMENT ON COLUMN prestamos.operaciones.desembolso_archivo_plano_id IS 'FK a integraciones.archivos_planos_bancarios — constraint añadido en V020';

-- Adjuntos opcionales durante la etapa CR
CREATE TABLE prestamos.operaciones_soportes (
  id            BIGSERIAL PRIMARY KEY,
  operacion_id  BIGINT    NOT NULL,
  documento_id  BIGINT    NOT NULL,
  CONSTRAINT fk_op_soportes_operacion  FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_op_soportes_documento  FOREIGN KEY (documento_id) REFERENCES prestamos.documentos(id)
);
