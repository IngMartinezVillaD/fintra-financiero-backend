# Runbook — Rollback de versión

**Uso:** cuando un despliegue en producción introduce un defecto crítico.  
**Tiempo objetivo de rollback:** < 15 min

---

## Precondiciones

- Versión anterior conocida (ver `APP_VERSION` en `.env.prod` o tags del registro Docker)
- Acceso SSH al servidor de producción o acceso a la plataforma de orquestación

## Procedimiento

### 1. Identificar la versión estable anterior

```bash
# Ver historial de imágenes desplegadas
docker images pluto/pluto-backend --format "{{.Tag}}\t{{.CreatedAt}}"

# O desde el registro
docker manifest inspect pluto/pluto-backend:v1.x.y
```

### 2. Rollback del backend

```bash
# Actualizar APP_VERSION en .env.prod
export APP_VERSION=v1.x.y   # versión estable anterior

# Bajar el servicio actual y levantar con la versión anterior
docker compose -f infra/docker/docker-compose.prod.yml up -d --no-deps backend
```

### 3. Rollback del frontend (si aplica)

```bash
# Actualizar imagen frontend en docker-compose.prod.yml o .env.prod
export FRONTEND_VERSION=v1.x.y
docker compose -f infra/docker/docker-compose.prod.yml up -d --no-deps frontend
```

### 4. Verificar salud tras rollback

```bash
# Health check backend
curl -s http://localhost:8080/pluto-service/actuator/health | jq .status

# Smoke tests rápidos
curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/pluto-service/api/v1/health-check
```

### 5. Si la BD requiere rollback (migraciones Flyway)

> ⚠️ **Solo si la migración fallida es reversible.**

```bash
# Identificar migración problemática
docker exec pluto-postgres-prod psql -U $DB_USER -d $DB_NAME \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;"

# Flyway no soporta rollback automático. Opciones:
# A) Restaurar backup (ver runbook-recuperacion-bd.md)
# B) Escribir script SQL inverso y ejecutar manualmente
#    Luego marcar la migración como reparada:
./mvnw flyway:repair
```

## Post-rollback

- [ ] Notificar al equipo vía Slack/correo el incidente y la versión activa.
- [ ] Abrir issue en GitHub con el defecto específico.
- [ ] No volver a desplegar la versión problemática hasta que el defecto esté corregido y con PR aprobado.
- [ ] Revisar pipeline CI para entender por qué los tests no detectaron el defecto.
