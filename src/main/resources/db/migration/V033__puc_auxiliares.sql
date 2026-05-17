-- ============================================================
-- V033 · PUC — Auxiliares (nivel = 8) para préstamos intercompañía
-- ============================================================

INSERT INTO prestamos.puc (codigo, nombre, tipo, naturaleza, nivel) VALUES

  -- ── ACTIVO · Caja ──────────────────────────────────────────
  ('11050501', 'CAJA GENERAL MONEDA NACIONAL',                   'ACTIVO', 'DEBITO',  8),
  ('11050502', 'CAJA MENOR',                                     'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · Bancos ────────────────────────────────────────
  ('11100501', 'BANCOLOMBIA CTAS CORRIENTES MN',                 'ACTIVO', 'DEBITO',  8),
  ('11100502', 'BANCO DE BOGOTA CTAS CORRIENTES MN',             'ACTIVO', 'DEBITO',  8),
  ('11100503', 'DAVIVIENDA CTAS CORRIENTES MN',                  'ACTIVO', 'DEBITO',  8),
  ('11100504', 'BBVA COLOMBIA CTAS CORRIENTES MN',               'ACTIVO', 'DEBITO',  8),
  ('11100505', 'BANCO DE OCCIDENTE CTAS CORRIENTES MN',          'ACTIVO', 'DEBITO',  8),
  ('11100506', 'ITAU COLOMBIA CTAS CORRIENTES MN',               'ACTIVO', 'DEBITO',  8),
  ('11100507', 'SCOTIABANK COLPATRIA CTAS CORRIENTES MN',        'ACTIVO', 'DEBITO',  8),
  ('11100508', 'BANCOLOMBIA CTAS AHORROS MN',                    'ACTIVO', 'DEBITO',  8),
  ('11100509', 'BANCO DE BOGOTA CTAS AHORROS MN',                'ACTIVO', 'DEBITO',  8),
  ('11100510', 'DAVIVIENDA CTAS AHORROS MN',                     'ACTIVO', 'DEBITO',  8),
  ('11101001', 'BANCOLOMBIA CTAS CORRIENTES ME',                 'ACTIVO', 'DEBITO',  8),
  ('11101002', 'CITIBANK CTAS CORRIENTES ME',                    'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · Clientes ──────────────────────────────────────
  ('13050501', 'CLIENTES NACIONALES ORDINARIOS',                 'ACTIVO', 'DEBITO',  8),
  ('13050502', 'CLIENTES NACIONALES RELACIONADOS',               'ACTIVO', 'DEBITO',  8),
  ('13051001', 'CLIENTES DEL EXTERIOR ORDINARIOS',               'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · Préstamos intercompañía por cobrar ────────────
  ('13550501', 'PRESTAMOS IC POR COBRAR - EMPRESA MATRIZ',       'ACTIVO', 'DEBITO',  8),
  ('13550502', 'PRESTAMOS IC POR COBRAR - FILIALES',             'ACTIVO', 'DEBITO',  8),
  ('13550503', 'PRESTAMOS IC POR COBRAR - EMPRESAS VINCULADAS',  'ACTIVO', 'DEBITO',  8),
  ('13550504', 'PRESTAMOS IC POR COBRAR - CAPITAL',              'ACTIVO', 'DEBITO',  8),
  ('13550505', 'PRESTAMOS IC POR COBRAR - INTERESES CAUSADOS',   'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · CxC socios y accionistas ──────────────────────
  ('13600501', 'CXC SOCIOS PRINCIPALES CAPITAL',                 'ACTIVO', 'DEBITO',  8),
  ('13600502', 'CXC SOCIOS PRINCIPALES INTERESES',               'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · Deudores varios ───────────────────────────────
  ('13800501', 'DEUDORES VARIOS NACIONALES ORDINARIOS',          'ACTIVO', 'DEBITO',  8),
  ('13800502', 'DEUDORES VARIOS NACIONALES RELACIONADOS',        'ACTIVO', 'DEBITO',  8),

  -- ── ACTIVO · Provisiones ───────────────────────────────────
  ('13990501', 'PROVISION GENERAL DEUDORES COMERCIALES',         'ACTIVO', 'CREDITO', 8),
  ('13990502', 'PROVISION INDIVIDUAL PRESTAMOS IC',              'ACTIVO', 'CREDITO', 8),

  -- ── PASIVO · Obligaciones financieras ─────────────────────
  ('21050501', 'PRESTAMOS BANCARIOS MN CORTO PLAZO',             'PASIVO', 'CREDITO', 8),
  ('21050502', 'PRESTAMOS BANCARIOS MN LARGO PLAZO',             'PASIVO', 'CREDITO', 8),
  ('21050503', 'SOBREGIROS BANCARIOS MN',                        'PASIVO', 'CREDITO', 8),

  -- ── PASIVO · Costos y gastos por pagar ────────────────────
  ('23350501', 'INTERESES POR PAGAR PRESTAMOS IC',               'PASIVO', 'CREDITO', 8),
  ('23350502', 'INTERESES POR PAGAR OBLIGACIONES FINANCIERAS',   'PASIVO', 'CREDITO', 8),
  ('23351001', 'COMISIONES POR PAGAR ENTIDADES FINANCIERAS',     'PASIVO', 'CREDITO', 8),

  -- ── PASIVO · Préstamos intercompañía por pagar ────────────
  ('23600501', 'PRESTAMOS IC POR PAGAR - EMPRESA MATRIZ',        'PASIVO', 'CREDITO', 8),
  ('23600502', 'PRESTAMOS IC POR PAGAR - FILIALES',              'PASIVO', 'CREDITO', 8),
  ('23600503', 'PRESTAMOS IC POR PAGAR - EMPRESAS VINCULADAS',   'PASIVO', 'CREDITO', 8),
  ('23600504', 'PRESTAMOS IC POR PAGAR - CAPITAL',               'PASIVO', 'CREDITO', 8),
  ('23600505', 'PRESTAMOS IC POR PAGAR - INTERESES CAUSADOS',    'PASIVO', 'CREDITO', 8),

  -- ── PASIVO · Retenciones ──────────────────────────────────
  ('23650501', 'RETENCION EN LA FUENTE HONORARIOS',              'PASIVO', 'CREDITO', 8),
  ('23650502', 'RETENCION EN LA FUENTE SERVICIOS',               'PASIVO', 'CREDITO', 8),
  ('23650503', 'RETENCION EN LA FUENTE INTERESES',               'PASIVO', 'CREDITO', 8),
  ('23650504', 'RETENCION EN LA FUENTE OTROS',                   'PASIVO', 'CREDITO', 8),

  -- ── PASIVO · Acreedores varios ────────────────────────────
  ('23800501', 'ACREEDORES VARIOS NACIONALES ORDINARIOS',        'PASIVO', 'CREDITO', 8),
  ('23800502', 'ACREEDORES VARIOS NACIONALES RELACIONADOS',      'PASIVO', 'CREDITO', 8),

  -- ── INGRESOS · Intereses préstamos ────────────────────────
  ('42100501', 'INTERESES PRESTAMOS ORDINARIOS GRAVADOS',        'INGRESO', 'CREDITO', 8),
  ('42100502', 'INTERESES PRESTAMOS ORDINARIOS EXENTOS',         'INGRESO', 'CREDITO', 8),
  ('42101001', 'INTERESES PRESTAMOS IC GRAVADOS',                'INGRESO', 'CREDITO', 8),
  ('42101002', 'INTERESES PRESTAMOS IC EXENTOS',                 'INGRESO', 'CREDITO', 8),
  ('42101003', 'INTERESES PRESTAMOS IC EMPRESA MATRIZ',          'INGRESO', 'CREDITO', 8),
  ('42101004', 'INTERESES PRESTAMOS IC FILIALES',                'INGRESO', 'CREDITO', 8),
  ('42101501', 'DIVIDENDOS RECIBIDOS NACIONALES',                'INGRESO', 'CREDITO', 8),
  ('42102001', 'DESCUENTOS FINANCIEROS OBTENIDOS',               'INGRESO', 'CREDITO', 8),

  -- ── GASTOS · Gastos bancarios ─────────────────────────────
  ('53050501', 'GASTOS BANCARIOS CUOTA MANEJO',                  'GASTO', 'DEBITO', 8),
  ('53050502', 'GASTOS BANCARIOS TRANSFERENCIAS',                'GASTO', 'DEBITO', 8),
  ('53050503', 'GASTOS BANCARIOS CERTIFICACIONES',               'GASTO', 'DEBITO', 8),
  ('53051001', 'COMISIONES BANCARIAS OPERACIONES',               'GASTO', 'DEBITO', 8),
  ('53051002', 'COMISIONES BANCARIAS FIDUCIA',                   'GASTO', 'DEBITO', 8),

  -- ── GASTOS · Intereses ────────────────────────────────────
  ('53051501', 'INTERESES OBLIGACIONES FINANCIERAS BANCOS',      'GASTO', 'DEBITO', 8),
  ('53051502', 'INTERESES SOBREGIROS BANCARIOS',                 'GASTO', 'DEBITO', 8),
  ('53052001', 'INTERESES PRESTAMOS IC PAGADOS MATRIZ',          'GASTO', 'DEBITO', 8),
  ('53052002', 'INTERESES PRESTAMOS IC PAGADOS FILIALES',        'GASTO', 'DEBITO', 8),
  ('53052003', 'INTERESES PRESTAMOS IC PAGADOS VINCULADAS',      'GASTO', 'DEBITO', 8),

  -- ── GASTOS · GMF ──────────────────────────────────────────
  ('53052501', 'GMF 4X1000 TRANSACCIONES BANCARIAS',             'GASTO', 'DEBITO', 8),
  ('53052502', 'GMF 4X1000 NO DEDUCIBLE ASUMIDO',                'GASTO', 'DEBITO', 8),

  -- ── GASTOS · Diferencia en cambio ─────────────────────────
  ('53053001', 'DIFERENCIA EN CAMBIO CUENTAS ACTIVAS',           'GASTO', 'DEBITO', 8),
  ('53053002', 'DIFERENCIA EN CAMBIO CUENTAS PASIVAS',           'GASTO', 'DEBITO', 8)

ON CONFLICT (codigo) DO NOTHING;
