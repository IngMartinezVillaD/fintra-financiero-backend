-- ============================================================
-- V014 — Bitácora de transiciones del pipeline
-- ============================================================

CREATE TABLE prestamos.eventos_pipeline (
  id              BIGSERIAL    PRIMARY KEY,
  operacion_id    BIGINT       NOT NULL,
  estado_anterior VARCHAR(20),
  estado_nuevo    VARCHAR(20)  NOT NULL,
  usuario_id      BIGINT,
  observacion     TEXT,
  ocurrido_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

  CONSTRAINT fk_eventos_operacion FOREIGN KEY (operacion_id) REFERENCES prestamos.operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_eventos_usuario   FOREIGN KEY (usuario_id)   REFERENCES seguridad.usuarios(id)
);

CREATE INDEX ix_eventos_pipeline_operacion ON prestamos.eventos_pipeline (operacion_id);
CREATE INDEX ix_eventos_pipeline_ocurrido  ON prestamos.eventos_pipeline (ocurrido_at DESC);

COMMENT ON TABLE prestamos.eventos_pipeline IS 'Auditoría inmutable de cada transición de estado en el pipeline de operaciones';
