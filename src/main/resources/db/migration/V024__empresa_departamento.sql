-- Agrega campo departamento a la tabla de empresas
ALTER TABLE prestamos.empresas
  ADD COLUMN IF NOT EXISTS departamento VARCHAR(100);
