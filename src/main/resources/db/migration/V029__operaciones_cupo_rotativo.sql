-- V029 · Vincula operaciones con cupo rotativo
ALTER TABLE prestamos.operaciones
  ADD COLUMN cupo_rotativo_id BIGINT
    REFERENCES prestamos.cupos_rotativos(id);

CREATE INDEX idx_operaciones_cupo_rotativo ON prestamos.operaciones(cupo_rotativo_id);

COMMENT ON COLUMN prestamos.operaciones.cupo_rotativo_id IS
  'Cupo rotativo de la prestataria que respalda esta operación. Obligatorio desde V029.';
