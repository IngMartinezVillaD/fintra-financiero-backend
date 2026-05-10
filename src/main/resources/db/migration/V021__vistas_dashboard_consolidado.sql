-- ============================================================
-- V021 — Vistas de dashboard y alertas
-- ============================================================

-- Solo operaciones con DS completado computan en el consolidado (regla de negocio #1)
CREATE OR REPLACE VIEW prestamos.vw_consolidado_financiero AS
SELECT
  ep.id                                         AS empresa_id,
  ep.codigo_interno,
  ep.razon_social,
  ep.nit,
  COUNT(DISTINCT o.id)                          AS total_operaciones,
  COALESCE(SUM(t.saldo_capital), 0)             AS saldo_capital_vigente,
  COALESCE(SUM(t.interes_calculado), 0)         AS intereses_causados_pendientes,
  COALESCE(SUM(d.monto), 0)                     AS total_desembolsado,
  MAX(d.fecha)                                  AS ultimo_desembolso_fecha
FROM
  prestamos.empresas ep
  LEFT JOIN prestamos.operaciones o
    ON (o.empresa_prestamista_id = ep.id OR o.empresa_prestataria_id = ep.id)
   AND o.estado_pipeline = 'DS'
   AND o.deleted_at IS NULL
  LEFT JOIN prestamos.tramos t
    ON t.operacion_id = o.id
   AND t.estado = 'EN_CURSO'
   AND t.deleted_at IS NULL
  LEFT JOIN prestamos.desembolsos d
    ON d.operacion_id = o.id
WHERE
  ep.deleted_at IS NULL
GROUP BY
  ep.id, ep.codigo_interno, ep.razon_social, ep.nit;

COMMENT ON VIEW prestamos.vw_consolidado_financiero
  IS 'Solo operaciones DS computan. Saldo capital e intereses siempre separados.';

-- Conteo de operaciones por etapa del pipeline
CREATE OR REPLACE VIEW prestamos.vw_pipeline_dashboard AS
SELECT
  estado_pipeline,
  COUNT(*)               AS total,
  MIN(fecha_creacion)    AS mas_antigua,
  MAX(fecha_creacion)    AS mas_reciente
FROM
  prestamos.operaciones
WHERE
  deleted_at IS NULL
  AND estado_pipeline NOT IN ('RECHAZADA', 'CANCELADA')
GROUP BY
  estado_pipeline
ORDER BY
  CASE estado_pipeline
    WHEN 'CR'  THEN 1
    WHEN 'AI'  THEN 2
    WHEN 'AE'  THEN 3
    WHEN 'FD'  THEN 4
    WHEN 'DS'  THEN 5
    ELSE 99
  END;

-- Tasas que vencen en los próximos 3 días (alertas)
CREATE OR REPLACE VIEW prestamos.vw_alertas_tasas_por_vencer AS
SELECT
  'GENERAL'            AS tipo,
  NULL::BIGINT         AS empresa_id,
  NULL::VARCHAR        AS empresa_razon_social,
  tipo_tasa            AS subtipo,
  vigencia_hasta,
  (vigencia_hasta - CURRENT_DATE) AS dias_restantes,
  estado
FROM
  prestamos.tasas_periodo
WHERE
  estado = 'APROBADA'
  AND vigencia_hasta BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '3 days'
  AND deleted_at IS NULL

UNION ALL

SELECT
  'ESPECIAL'           AS tipo,
  te.empresa_id,
  ep.razon_social      AS empresa_razon_social,
  'TASA_ESPECIAL'      AS subtipo,
  te.vigencia_hasta,
  (te.vigencia_hasta - CURRENT_DATE) AS dias_restantes,
  te.estado
FROM
  prestamos.tasas_especiales_empresa te
  JOIN prestamos.empresas ep ON ep.id = te.empresa_id
WHERE
  te.estado = 'VIGENTE'
  AND te.vigencia_hasta BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '3 days'
  AND te.deleted_at IS NULL

ORDER BY
  dias_restantes ASC;

COMMENT ON VIEW prestamos.vw_alertas_tasas_por_vencer
  IS 'Tasas vencidas/no aprobadas bloquean el sistema. Tasa especial vencida bloquea solo esa empresa.';
