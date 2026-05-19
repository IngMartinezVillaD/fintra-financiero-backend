# Visión del Sistema — Fintra Financiero Módulo 9

## Contexto

Fintra S.A.S. necesita digitalizar y automatizar el proceso de **préstamos intercompañía** entre empresas del grupo. El sistema debe manejar el ciclo de vida completo: creación → aprobación interna → aceptación empresa → firma digital → desembolso → seguimiento de abonos → liquidación mensual.

## Diagrama C4 — Nivel 1 (System Context)

```mermaid
C4Context
  title Contexto del Sistema — Pluto Módulo 9

  Person(tesoreria, "Tesorería", "Gestiona operaciones de préstamo")
  Person(aprobador, "Aprobador", "Aprueba internamente")
  Person(empresa, "Empresa Receptora", "Acepta y gestiona el préstamo")

  System(pluto, "Pluto", "Módulo 9 – Préstamos Intercompañía")

  System_Ext(thomas, "Thomas Signe", "Firma digital de documentos")
  System_Ext(apotheosys, "Apotheosys ERP", "Contabilización automática")
  System_Ext(siigo, "SIIGO ERP", "Contabilización alternativa")
  System_Ext(bancos, "Bancos (ACH)", "Desembolsos bancarios")

  Rel(tesoreria, pluto, "Crea y gestiona operaciones")
  Rel(aprobador, pluto, "Aprueba operaciones")
  Rel(empresa, pluto, "Acepta préstamos")
  Rel(pluto, thomas, "Envía documentos para firma")
  Rel(pluto, apotheosys, "Envía asientos contables")
  Rel(pluto, siigo, "Envía asientos contables")
  Rel(pluto, bancos, "Genera archivos ACH")
```

## Diagrama C4 — Nivel 2 (Container)

```mermaid
C4Container
  title Contenedores — Fintra Financiero

  Person(user, "Usuario")

  Container(frontend, "Frontend Angular", "Angular 21 + NgRx SignalStore + Tailwind", "SPA en el navegador")
  Container(backend, "Backend Spring Boot", "Spring Boot 4.0.6 + Java 21", "API REST / lógica de negocio")
  ContainerDb(postgres, "PostgreSQL 18", "Base de datos relacional", "Datos de operaciones, auditoría, usuarios")
  Container(redis, "Redis 7", "Cache distribuido", "Idempotencia y cache de tasas")

  Rel(user, frontend, "Usa", "HTTPS")
  Rel(frontend, backend, "Llama API", "HTTP/REST + JWT")
  Rel(backend, postgres, "Lee/Escribe", "JDBC/JPA")
  Rel(backend, redis, "Cache/Idempotencia", "Redis Protocol")
```
