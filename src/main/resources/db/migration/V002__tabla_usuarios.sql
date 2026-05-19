-- ============================================================
-- V002 — Tabla de usuarios y roles (esquema seguridad)
-- ============================================================

CREATE TABLE seguridad.usuarios (
  id              BIGSERIAL PRIMARY KEY,
  username        VARCHAR(100)  NOT NULL UNIQUE,
  password        VARCHAR(255)  NOT NULL,
  nombre          VARCHAR(200)  NOT NULL,
  email           VARCHAR(255)  NOT NULL UNIQUE,
  activo          BOOLEAN       NOT NULL DEFAULT TRUE,
  intentos_fallidos INTEGER     NOT NULL DEFAULT 0,
  bloqueado_hasta TIMESTAMPTZ,
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by      VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_by      VARCHAR(100)  NOT NULL DEFAULT 'system',
  version         BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE seguridad.roles (
  id     BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE  -- ADMIN, TESORERIA, APROBADOR, etc.
);

CREATE TABLE seguridad.usuario_roles (
  usuario_id BIGINT NOT NULL REFERENCES seguridad.usuarios(id),
  rol_id     BIGINT NOT NULL REFERENCES seguridad.roles(id),
  PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE seguridad.auditoria_login (
  id          BIGSERIAL PRIMARY KEY,
  username    VARCHAR(100) NOT NULL,
  exitoso     BOOLEAN      NOT NULL,
  ip          VARCHAR(45),
  user_agent  TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Datos iniciales
INSERT INTO seguridad.roles (nombre) VALUES
  ('ADMIN'), ('TESORERIA'), ('APROBADOR'),
  ('EMPRESA_RECEPTORA'), ('CONTABILIDAD'), ('CONSULTA');

-- Usuario admin inicial (password: Admin123!)
INSERT INTO seguridad.usuarios (username, password, nombre, email, created_by, updated_by)
VALUES ('admin', '$2b$12$DR7chkw85.golIFqWcX6vu.RugbcWPmgvK2oR951C6tnjmnsdmlIu',
        'Administrador', 'admin@pluto.co', 'system', 'system');

INSERT INTO seguridad.usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM seguridad.usuarios u, seguridad.roles r
WHERE u.username = 'admin' AND r.nombre = 'ADMIN';
