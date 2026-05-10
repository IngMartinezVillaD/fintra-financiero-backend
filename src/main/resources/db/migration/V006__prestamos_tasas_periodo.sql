-- ============================================================
-- V006 — Tasas de período (generales)
-- ============================================================

CREATE TABLE prestamos.tasas_periodo (
  id                                  BIGSERIAL     PRIMARY KEY,
  anio                                SMALLINT      NOT NULL,
  mes                                 SMALLINT      NOT NULL CHECK (mes BETWEEN 1 AND 12),
  tipo_tasa                           VARCHAR(30)   NOT NULL,
  valor_porcentaje_efectivo_anual     NUMERIC(8,4)  NOT NULL CHECK (valor_porcentaje_efectivo_anual > 0),
  valor_porcentaje_mensual            NUMERIC(8,4)  NOT NULL CHECK (valor_porcentaje_mensual > 0),
  vigencia_desde                      DATE          NOT NULL,
  vigencia_hasta                      DATE          NOT NULL,
  estado                              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
  aprobado_por_usuario_id             BIGINT,
  aprobado_at                         TIMESTAMPTZ,
  observacion_aprobacion              TEXT,
  created_at                          TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                          TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by                          VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by                          VARCHAR(100)  NOT NULL DEFAULT 'system',
  version                             BIGINT        NOT NULL DEFAULT 0,
  deleted_at                          TIMESTAMPTZ,

  CONSTRAINT uq_tasas_periodo_anio_mes_tipo     UNIQUE (anio, mes, tipo_tasa),
  CONSTRAINT chk_tasas_periodo_tipo             CHECK (tipo_tasa IN ('USURA', 'COMERCIAL_VIGENTE', 'PRESUNTA_FISCAL')),
  CONSTRAINT chk_tasas_periodo_estado           CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
  CONSTRAINT chk_tasas_periodo_vigencia         CHECK (vigencia_hasta >= vigencia_desde),
  CONSTRAINT fk_tasas_periodo_aprobador         FOREIGN KEY (aprobado_por_usuario_id) REFERENCES seguridad.usuarios(id)
);

COMMENT ON TABLE prestamos.tasas_periodo IS 'Tasas generales por mes: USURA, COMERCIAL_VIGENTE, PRESUNTA_FISCAL. Tasas vencidas/no aprobadas bloquean el sistema.';
