-- ============================================================
-- V011 — Abonos a operaciones
-- ============================================================

-- Regla: al registrar un abono, se liquida el tramo anterior ANTES de aplicar el pago.
-- Orden de aplicación: 1° intereses causados, 2° capital.
CREATE TABLE prestamos.abonos (
  id                      BIGSERIAL     PRIMARY KEY,
  operacion_id            BIGINT        NOT NULL,
  fecha                   DATE          NOT NULL,
  monto_total             NUMERIC(19,6) NOT NULL CHECK (monto_total > 0),
  numero_comprobante      VARCHAR(60)   NOT NULL,
  aplicado_a_intereses    NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (aplicado_a_intereses >= 0),
  aplicado_a_capital      NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (aplicado_a_capital >= 0),
  tramo_liquidado_id      BIGINT,                                   -- tramo cerrado antes de aplicar el abono
  usuario_id              BIGINT        NOT NULL,
  observaciones           TEXT,
  created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by              VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by              VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                 BIGINT        NOT NULL DEFAULT 0,

  CONSTRAINT fk_abonos_operacion       FOREIGN KEY (operacion_id)       REFERENCES prestamos.operaciones(id) ON DELETE RESTRICT,
  CONSTRAINT fk_abonos_tramo_liquidado FOREIGN KEY (tramo_liquidado_id) REFERENCES prestamos.tramos(id),
  CONSTRAINT fk_abonos_usuario         FOREIGN KEY (usuario_id)         REFERENCES seguridad.usuarios(id)
);

COMMENT ON TABLE prestamos.abonos IS 'Orden aplicación: intereses causados primero, luego capital. Tramo anterior liquidado antes de registrar el abono.';
