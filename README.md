# fintra-financiero-backend

> **Módulo 9 – Préstamos Intercompañía** | Backend · Fintra S.A.S.

Spring Boot 4.0.6 · Java 21 · PostgreSQL 18 · Maven · Clean Architecture

## Stack

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 4.0.6 · Java 21 |
| Seguridad | Spring Security + JWT (jjwt 0.12.5) |
| Persistencia | Spring Data JPA + Flyway + PostgreSQL 18 |
| Cache | Caffeine L1 + Redis L2 |
| API Docs | SpringDoc OpenAPI 3 |
| Observabilidad | Micrometer + Prometheus + Logback JSON |
| Tests | JUnit 5 + Testcontainers + ArchUnit |

## Quickstart (5 comandos)

```bash
# 1. Clonar
git clone <url-repo> && cd fintra-financiero-backend

# 2. Levantar infraestructura local (Postgres + Redis + pgAdmin + Mailhog)
docker compose -f infra/docker/docker-compose.dev.yml up -d

# 3. Ejecutar backend en perfil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 4. Verificar salud
curl http://localhost:8080/actuator/health

# 5. Ver Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Puertos de desarrollo

| Servicio | Puerto |
|---------|--------|
| Backend API | 8080 |
| PostgreSQL | 5432 |
| pgAdmin | 5050 |
| Mailhog UI | 8025 |
| Redis | 6379 |
| Prometheus | /actuator/prometheus |

## Estructura

```
co.fintra.financiero
├── domain/         ← Entidades, VOs, puertos, reglas puras (sin Spring)
├── application/    ← Casos de uso, DTOs, orquestación
├── infrastructure/ ← JPA, integraciones, security, config
└── interfaces/     ← REST controllers, webhooks, advice
```

## Frontend

→ [fintra-financiero-frontend](../fintra-financiero-frontend)
