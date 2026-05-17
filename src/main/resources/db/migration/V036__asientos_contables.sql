-- Asientos contables generados por el motor de contabilización
CREATE TABLE prestamos.asientos_contables (
  id            BIGSERIAL       PRIMARY KEY,
  tipo_origen   VARCHAR(20)     NOT NULL CHECK (tipo_origen IN ('LIQUIDACION','DESEMBOLSO')),
  origen_id     BIGINT          NOT NULL,
  empresa_id    BIGINT          NOT NULL REFERENCES prestamos.empresas(id),
  interfaz_id   BIGINT          NOT NULL REFERENCES prestamos.interfaces_contables(id),
  fecha         DATE            NOT NULL,
  descripcion   VARCHAR(300),
  estado        VARCHAR(20)     NOT NULL DEFAULT 'GENERADO' CHECK (estado IN ('GENERADO','ANULADO')),
  created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
  created_by    VARCHAR(100)    NOT NULL DEFAULT 'system'
);

CREATE INDEX idx_asientos_origen ON prestamos.asientos_contables (tipo_origen, origen_id);
CREATE INDEX idx_asientos_empresa ON prestamos.asientos_contables (empresa_id);

-- Líneas débito/crédito de cada asiento
CREATE TABLE prestamos.asientos_contables_detalle (
  id              BIGSERIAL     PRIMARY KEY,
  asiento_id      BIGINT        NOT NULL REFERENCES prestamos.asientos_contables(id) ON DELETE CASCADE,
  orden           SMALLINT      NOT NULL,
  cuenta_puc_id   BIGINT        NOT NULL REFERENCES prestamos.puc(id),
  naturaleza      VARCHAR(10)   NOT NULL CHECK (naturaleza IN ('DEBITO','CREDITO')),
  monto           NUMERIC(19,6) NOT NULL,
  glosa           VARCHAR(300)
);

CREATE INDEX idx_asientos_detalle_asiento ON prestamos.asientos_contables_detalle (asiento_id);

COMMENT ON TABLE prestamos.asientos_contables         IS 'Asientos contables generados automáticamente por liquidaciones y desembolsos';
COMMENT ON TABLE prestamos.asientos_contables_detalle IS 'Líneas débito/crédito de cada asiento contable';
