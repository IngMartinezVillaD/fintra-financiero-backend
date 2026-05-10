-- ============================================================
-- V001 — Creación de esquemas base
-- Módulo 9: Préstamos Intercompañía · Fintra S.A.S.
-- ============================================================

CREATE SCHEMA IF NOT EXISTS prestamos;
CREATE SCHEMA IF NOT EXISTS auditoria;
CREATE SCHEMA IF NOT EXISTS integraciones;
CREATE SCHEMA IF NOT EXISTS seguridad;

-- Extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

COMMENT ON SCHEMA prestamos      IS 'Operaciones de préstamos intercompañía';
COMMENT ON SCHEMA auditoria      IS 'Trazabilidad y auditoría de cambios';
COMMENT ON SCHEMA integraciones  IS 'Tablas de control para integraciones ERP';
COMMENT ON SCHEMA seguridad      IS 'Usuarios, roles y auditoría de acceso';
