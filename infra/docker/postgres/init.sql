-- Script de inicialización PostgreSQL
-- Se ejecuta solo la primera vez que se crea el contenedor

-- Configuraciones de rendimiento básicas
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET log_min_duration_statement = '1000';
ALTER SYSTEM SET log_checkpoints = 'on';

SELECT pg_reload_conf();
