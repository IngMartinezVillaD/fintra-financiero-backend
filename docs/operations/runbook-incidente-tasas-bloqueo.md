# Runbook — Incidente: Bloqueo global del sistema por tasas vencidas

**Severidad:** Alta  
**Afectación:** No se pueden crear nuevas operaciones ni confirmar desembolsos.  
**Tiempo de resolución objetivo:** < 30 min

---

## Síntomas

- Banner rojo visible en el frontend: "Sistema bloqueado — no hay tasa vigente"
- `POST /api/v1/operaciones` devuelve 200 con mensaje: `"No se puede crear la operación: No existe tasa COMERCIAL_VIGENTE vigente para hoy"`
- `GET /api/v1/dashboard` muestra `alertas` con `diasRestantes <= 0`

## Diagnóstico

```bash
# 1. Verificar tasas vigentes hoy
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/pluto-service/api/v1/tasas-periodo

# 2. Buscar tasas con estado APROBADA y vigencia_hasta < HOY
# Salida esperada en bloqueo: [] (lista vacía)
```

## Resolución

### Opción A — Aprobar tasa nueva (camino normal)

1. TESORERIA registra nueva tasa del período:
   ```
   POST /api/v1/tasas-periodo
   { "anio": YYYY, "mes": MM, "tipoTasa": "COMERCIAL_VIGENTE",
     "valorPorcentajeEfectivoAnual": X.XX, "valorPorcentajeMensual": Y.YY,
     "vigenciaDesde": "YYYY-MM-01", "vigenciaHasta": "YYYY-MM-31" }
   ```

2. APROBADOR aprueba:
   ```
   PATCH /api/v1/tasas-periodo/{id}/aprobar
   ```

3. Verificar que el bloqueo se levantó:
   ```
   GET /api/v1/tasas-periodo/estado-bloqueo
   # Esperado: { "estado": "LIBRE" }
   ```

### Opción B — Si el ambiente de aprobación no está disponible (contingencia)

Solo aplicar con autorización de Gerencia y registro en bitácora:

```sql
-- En BD productiva (requiere DBA)
UPDATE prestamos.tasas_periodo
SET estado = 'APROBADA',
    vigencia_hasta = CURRENT_DATE + INTERVAL '7 days',
    updated_at = NOW()
WHERE tipo_tasa = 'COMERCIAL_VIGENTE'
  AND estado = 'PENDIENTE'
  AND anio = EXTRACT(YEAR FROM CURRENT_DATE)
  AND mes = EXTRACT(MONTH FROM CURRENT_DATE);
```

## Post-incidente

- [ ] Registrar en bitácora con timestamp, causa raíz y resolución aplicada.
- [ ] Revisar por qué no se renovó la tasa a tiempo (proceso de alerta: 3 días antes).
- [ ] Verificar que `vw_alertas_tasas_por_vencer` estaba emitiendo alertas correctamente.
- [ ] Revisar configuración de alertas en Grafana (umbral `diasRestantes <= 3`).
