-- ============================================================
-- V020 — Índices adicionales + FK constraints diferidas
-- ============================================================

-- ── FK diferidas (referencias a tablas creadas en V015) ──────

-- operaciones.firma_digital_documento_id -> thomas_signe_solicitudes
ALTER TABLE prestamos.operaciones
  ADD CONSTRAINT fk_operaciones_firma_digital
  FOREIGN KEY (firma_digital_documento_id)
  REFERENCES integraciones.thomas_signe_solicitudes(id);

-- operaciones.desembolso_archivo_plano_id -> archivos_planos_bancarios
ALTER TABLE prestamos.operaciones
  ADD CONSTRAINT fk_operaciones_archivo_plano
  FOREIGN KEY (desembolso_archivo_plano_id)
  REFERENCES integraciones.archivos_planos_bancarios(id);

-- desembolsos.archivo_plano_id -> archivos_planos_bancarios
ALTER TABLE prestamos.desembolsos
  ADD CONSTRAINT fk_desembolsos_archivo_plano
  FOREIGN KEY (archivo_plano_id)
  REFERENCES integraciones.archivos_planos_bancarios(id);

-- tramos.liquidacion_id -> liquidaciones_mensuales (V012 existe, agregar FK aquí)
ALTER TABLE prestamos.tramos
  ADD CONSTRAINT fk_tramos_liquidacion
  FOREIGN KEY (liquidacion_id)
  REFERENCES prestamos.liquidaciones_mensuales(id);

-- ── Índices por búsqueda frecuente ───────────────────────────

-- Empresas
CREATE INDEX ix_empresas_estado         ON prestamos.empresas (estado) WHERE deleted_at IS NULL;
CREATE INDEX ix_empresas_nit            ON prestamos.empresas (nit);
CREATE INDEX ix_empresas_codigo         ON prestamos.empresas (codigo_interno);

-- Cuentas bancarias
CREATE INDEX ix_empresa_cuentas_empresa ON prestamos.empresa_cuentas_bancarias (empresa_id);

-- Tasas período
CREATE INDEX ix_tasas_periodo_anio_mes  ON prestamos.tasas_periodo (anio, mes);
CREATE INDEX ix_tasas_periodo_estado    ON prestamos.tasas_periodo (estado);

-- Tasas especiales
CREATE INDEX ix_tasas_esp_empresa       ON prestamos.tasas_especiales_empresa (empresa_id);
CREATE INDEX ix_tasas_esp_vigencia      ON prestamos.tasas_especiales_empresa (vigencia_desde, vigencia_hasta);

-- Operaciones
CREATE INDEX ix_operaciones_prestamista ON prestamos.operaciones (empresa_prestamista_id);
CREATE INDEX ix_operaciones_prestataria ON prestamos.operaciones (empresa_prestataria_id);
CREATE INDEX ix_operaciones_estado      ON prestamos.operaciones (estado_pipeline) WHERE deleted_at IS NULL;
CREATE INDEX ix_operaciones_fecha       ON prestamos.operaciones (fecha_creacion DESC);

-- Tramos
CREATE INDEX ix_tramos_operacion        ON prestamos.tramos (operacion_id);
CREATE INDEX ix_tramos_estado           ON prestamos.tramos (estado) WHERE deleted_at IS NULL;
CREATE INDEX ix_tramos_liquidacion      ON prestamos.tramos (liquidacion_id) WHERE liquidacion_id IS NOT NULL;

-- Desembolsos
CREATE INDEX ix_desembolsos_operacion   ON prestamos.desembolsos (operacion_id);
CREATE INDEX ix_desembolsos_fecha       ON prestamos.desembolsos (fecha DESC);

-- Abonos
CREATE INDEX ix_abonos_operacion        ON prestamos.abonos (operacion_id);
CREATE INDEX ix_abonos_fecha            ON prestamos.abonos (fecha DESC);

-- GMF e interés presunto
CREATE INDEX ix_gmf_empresa_anio_mes    ON prestamos.gmf_movimientos (empresa_id, anio, mes);
CREATE INDEX ix_intpres_empresa_anio    ON prestamos.interes_presunto_movimientos (empresa_id, anio, mes);

-- Integraciones
CREATE INDEX ix_thomas_estado           ON integraciones.thomas_signe_solicitudes (estado);
CREATE INDEX ix_archivos_banco_fecha    ON integraciones.archivos_planos_bancarios (banco_codigo, fecha_generacion DESC);

-- Auditoría history (búsqueda por entidad original)
CREATE INDEX ix_hist_usuarios_id        ON auditoria.usuarios_history (id);
CREATE INDEX ix_hist_empresas_id        ON auditoria.empresas_history (id);
CREATE INDEX ix_hist_operaciones_id     ON auditoria.operaciones_history (id);
CREATE INDEX ix_hist_tramos_id          ON auditoria.tramos_history (id);
