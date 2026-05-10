-- ============================================================
-- V010 — Desembolsos
-- ============================================================

-- archivo_plano_id referencia integraciones.archivos_planos_bancarios (V015)
-- FK se añade en V020
CREATE TABLE prestamos.desembolsos (
  id               BIGSERIAL     PRIMARY KEY,
  operacion_id     BIGINT        NOT NULL,
  monto            NUMERIC(19,6) NOT NULL CHECK (monto > 0),
  fecha            DATE          NOT NULL,
  archivo_plano_id BIGINT,                                          -- FK -> integraciones.archivos_planos_bancarios (V020)
  gmf_calculado    NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (gmf_calculado >= 0),
  gmf_aplica       BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by       VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by       VARCHAR(100)  NOT NULL DEFAULT 'system',
  version          BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT fk_desembolsos_operacion FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id) ON DELETE RESTRICT
);

COMMENT ON TABLE  prestamos.desembolsos                    IS 'Solo operaciones con desembolso DS completado computan en el consolidado financiero';
COMMENT ON COLUMN prestamos.desembolsos.archivo_plano_id   IS 'FK a integraciones.archivos_planos_bancarios — constraint añadido en V020';
