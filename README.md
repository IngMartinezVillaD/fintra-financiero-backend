# pluto-backend

> **Módulo Préstamos Intercompañía** | Backend · Fintra S.A.S.

Spring Boot 3.3.8 · Java 21 · PostgreSQL · Maven

## Stack

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 3.3.8 · Java 21 |
| Seguridad | Spring Security + JWT (jjwt 0.12.5) |
| Persistencia | Spring Data JPA · Flyway · PostgreSQL |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Utilidades | Lombok · MapStruct · OpenPDF |
| Observabilidad | Logback JSON (logstash-logback-encoder) |
| Tests | JUnit 5 |

## Quickstart

```bash
# 1. Levantar infraestructura local
docker compose -f infra/docker/docker-compose.dev.yml up -d

# 2. Ejecutar con perfil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 3. Verificar salud
curl http://localhost:8080/pluto-service/api/v1/health

# 4. Swagger UI
open http://localhost:8080/pluto-service/swagger-ui.html
```

## Puertos

| Servicio | Puerto |
|---------|--------|
| Backend API | `8080` · context path `/pluto-service` |
| PostgreSQL | `5432` |
| pgAdmin | `5050` |

## Estructura de paquetes

```
co.pluto
├── config/            ← Security, JWT, auditoría, OpenAPI
├── controllers/       ← REST endpoints + BaseController
├── dto/
│   ├── request/       ← Payloads de entrada (validados con @Valid)
│   └── response/      ← DTOs de salida
├── models/
│   ├── entity/        ← Entidades JPA (@Table schema=prestamos)
│   └── repositories/  ← Spring Data JPA + JpaSpecificationExecutor
├── services/
│   ├── interfaces/    ← Contratos de servicio
│   └── impl/          ← Implementaciones + motores de cálculo
├── infrastructure/    ← Integraciones externas (Bitrix24, etc.)
└── utils/             ← Excepciones de negocio, value objects
```

## Módulos funcionales

| Módulo | Endpoints base |
|--------|---------------|
| Autenticación | `/api/v1/auth` |
| Empresas | `/api/v1/empresas` |
| Operaciones | `/api/v1/operaciones` |
| Desembolsos | `/api/v1/desembolsos` |
| Seguimiento | `/api/v1/operaciones/seguimiento` |
| Cupos rotativos | `/api/v1/cupos-rotativos` |
| Saldos iniciales | `/api/v1/saldos-iniciales` |
| Liquidación mensual | `/api/v1/liquidaciones-mensuales` |
| Liquidación diaria | `/api/v1/liquidaciones-diarias` |
| PUC | `/api/v1/puc` |
| Interfaces contables | `/api/v1/interfaces-contables` |
| Asientos contables | `/api/v1/asientos-contables` |
| Controles (GMF / Interés presunto) | `/api/v1/controles` |
| Dashboard / Reportes | `/api/v1/dashboard` |
| Tasas período | `/api/v1/tasas-periodo` |

## Migraciones Flyway

Las migraciones viven en `src/main/resources/db/migration/` con el prefijo `Vxxx__`.  
Schema principal: `prestamos`.

## Roles de seguridad

`ADMIN` · `TESORERIA` · `APROBADOR` · `CONTABILIDAD` · `CONSULTA` · `EMPRESA_RECEPTORA`

## Frontend

→ [pluto-frontend](../pluto-frontend)
