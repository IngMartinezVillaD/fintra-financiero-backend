# Runbook — Liquidación mensual interrumpida o fallida

**Uso:** cuando el cálculo de liquidación mensual falla o queda en estado inconsistente.  
**Tiempo objetivo:** < 1 hora

---

## Estados posibles y acción

| Estado actual | Problema | Acción |
|---|---|---|
| `BORRADOR` | Cálculo nunca ejecutado | Ejecutar `POST /liquidaciones-mensuales/{id}/calcular` |
| `BORRADOR` (tras revertir) | Revertido correctamente | Volver a calcular normalmente |
| `PENDIENTE_APROBACION` | Error en aprobación | Diagnosticar con logs, reintentar aprobación |
| `APROBADA` (falla en plantillas) | Error en generación Excel | Regenerar sin cambiar estado |

---

## Diagnóstico

```bash
# 1. Ver estado de la liquidación
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales

# 2. Ver detalle de la liquidación con ID específico
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales/{id}

# 3. Revisar logs del motor de liquidación
docker logs fintra-backend-prod | grep -i "liquidacion\|motor\|ERROR" | tail -50
```

## Resolución

### Caso A — Cálculo fallido parcialmente (algunas operaciones calculadas)

El motor es **idempotente**: las operaciones ya calculadas se saltean en una segunda ejecución.

```bash
# Reintentar el cálculo (idempotente — no duplica registros)
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales/{id}/calcular
```

### Caso B — Datos inconsistentes (tramos mal cerrados)

```bash
# Revertir la liquidación (restaura tramos a EN_CURSO)
curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales/{id}/revertir

# Verificar que los tramos volvieron a EN_CURSO
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/fintra-financiero-service/api/v1/operaciones/{opId}/tramos"

# Volver a calcular
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales/{id}/calcular
```

### Caso C — No existe liquidación para el mes

```bash
# Crear nueva liquidación
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"anio": YYYY, "mes": MM}' \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales
```

## Verificación final

```bash
# Verificar que la liquidación está en PENDIENTE_APROBACION con totales > 0
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fintra-financiero-service/api/v1/liquidaciones-mensuales/{id} \
  | jq '{estado: .data.estado, totalIntereses: .data.totalInteresesLiquidados, detalle: (.data.detalle | length)}'
```

## Notas importantes

- **La reversión no se puede hacer en estado `APROBADA`.** Si la liquidación fue aprobada y tiene errores, contactar al equipo de TI para análisis manual.
- Los tramos con `liquidacion_id` asignado son los que pertenecen a esa liquidación.
- En caso de duda, **no ejecutar SQL directo** sin revisión del equipo de desarrollo.
