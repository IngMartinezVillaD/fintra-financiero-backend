-- Hibernate valida tipos exactos: bpchar (CHAR) != varchar.
-- Convierte todas las columnas CHAR del catálogo geográfico a VARCHAR.

ALTER TABLE catalogos.paises
  ALTER COLUMN codigo_iso2 TYPE VARCHAR(2),
  ALTER COLUMN codigo_iso3 TYPE VARCHAR(3);

ALTER TABLE catalogos.departamentos
  ALTER COLUMN codigo_dane TYPE VARCHAR(2);

ALTER TABLE catalogos.ciudades
  ALTER COLUMN codigo_dane    TYPE VARCHAR(5),
  ALTER COLUMN codigo_postal  TYPE VARCHAR(6);
