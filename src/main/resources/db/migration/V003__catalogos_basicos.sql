-- ============================================================
-- V003 — Catálogos básicos + función utilitaria set_updated_at
-- ============================================================

-- Completar columnas faltantes en seguridad.usuarios
ALTER TABLE seguridad.usuarios
  ADD COLUMN IF NOT EXISTS deleted_at      TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS ultima_conexion TIMESTAMPTZ;

-- Función genérica reutilizada por todos los triggers de updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

-- ── Catálogo de bancos ────────────────────────────────────────
CREATE TABLE prestamos.bancos (
  id                    BIGSERIAL    PRIMARY KEY,
  codigo                VARCHAR(20)  NOT NULL,
  nombre                VARCHAR(150) NOT NULL,
  formato_archivo_plano VARCHAR(50),
  activo                BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by            VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_by            VARCHAR(100) NOT NULL DEFAULT 'system',
  version               BIGINT       NOT NULL DEFAULT 0,
  CONSTRAINT uq_bancos_codigo UNIQUE (codigo)
);

COMMENT ON TABLE prestamos.bancos IS 'Catálogo de bancos — formatos ACH pendientes de Tesorería';

-- ── Catálogo de cuentas contables (stub) ─────────────────────
-- Códigos definitivos pendientes de Contabilidad / Apotheosys / SIIGO
CREATE TABLE prestamos.cuentas_contables (
  id          BIGSERIAL    PRIMARY KEY,
  codigo      VARCHAR(30)  NOT NULL,
  descripcion VARCHAR(200) NOT NULL,
  tipo        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO',
  activa      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  created_by  VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_by  VARCHAR(100) NOT NULL DEFAULT 'system',
  version     BIGINT       NOT NULL DEFAULT 0,
  CONSTRAINT uq_cuentas_contables_codigo UNIQUE (codigo)
);

COMMENT ON TABLE prestamos.cuentas_contables IS 'Stub — códigos contables definitivos pendientes de Tesorería/Contabilidad';
