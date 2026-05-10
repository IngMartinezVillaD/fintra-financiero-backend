# Decisiones de Arquitectura (ADRs)

## ADR-001: Clean Architecture / Hexagonal

**Estado:** Aceptada  
**Contexto:** Sistema financiero crítico con múltiples integraciones ERP.  
**Decisión:** Clean Architecture con 4 capas: `domain`, `application`, `infrastructure`, `interfaces`.  
**Consecuencias:** El dominio es framework-agnóstico. Los tests de dominio son rápidos. Cambiar ORM o web framework no afecta la lógica de negocio.

## ADR-002: Spring Boot 4.0.6 + Java 21

**Estado:** Aceptada  
**Decisión:** Spring Boot 4.0.6 con Java 21 LTS, virtual threads activados.  
**Consecuencias:** Virtual threads eliminan el pool de hilos bloqueante. Mejor rendimiento en I/O intensivo (llamadas a ERPs).

## ADR-003: PostgreSQL 18

**Estado:** Aceptada  
**Decisión:** PostgreSQL 18 como única BD relacional. `NUMERIC(19,6)` para montos financieros.  
**Consecuencias:** Cero riesgo de errores de punto flotante. Soporte completo a transacciones ACID.

## ADR-004: JWT stateless + Refresh rotativo

**Estado:** Aceptada  
**Decisión:** Access token 15 min, refresh 12h, rotativo. Tokens en `sessionStorage` (frontend).  
**Consecuencias:** Sin estado en el servidor. Revocación solo en expiración o cambio de contraseña.

## ADR-005: Polirepo (2 repositorios independientes)

**Estado:** Aceptada  
**Decisión:** Backend y frontend en repos Git separados. Comunicación solo por HTTP/REST + OpenAPI.  
**Consecuencias:** Equipos independientes, releases independientes, pipelines CI/CD separados. Sin riesgo de acoplamiento de código.
