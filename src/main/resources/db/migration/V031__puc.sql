-- ============================================================
-- V031 · Plan Único de Cuentas (PUC Colombia - Decreto 2649)
-- ============================================================

CREATE TABLE prestamos.puc (
  id          BIGSERIAL        PRIMARY KEY,
  codigo      VARCHAR(8)       NOT NULL UNIQUE,
  nombre      VARCHAR(200)     NOT NULL,
  tipo        VARCHAR(30)      NOT NULL
                CHECK (tipo IN ('ACTIVO','PASIVO','PATRIMONIO','INGRESO','GASTO',
                                'COSTO_VENTA','COSTO_PRODUCCION','ORDEN_DEUDORA','ORDEN_ACREEDORA')),
  naturaleza  VARCHAR(10)      NOT NULL CHECK (naturaleza IN ('DEBITO','CREDITO')),
  nivel       SMALLINT         NOT NULL,
  activa      BOOLEAN          NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  created_by  VARCHAR(100)     NOT NULL DEFAULT 'system',
  updated_by  VARCHAR(100)     NOT NULL DEFAULT 'system',
  version     BIGINT           NOT NULL DEFAULT 0,
  deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_puc_codigo ON prestamos.puc(codigo);
CREATE INDEX idx_puc_nivel  ON prestamos.puc(nivel);

COMMENT ON TABLE prestamos.puc IS
  'Plan Único de Cuentas colombiano (Decreto 2649). Niveles: 1=Clase, 2=Grupo, 4=Cuenta, 6=Subcuenta, 8=Auxiliar.';

-- ============================================================
-- SEED — Clases (nivel = 1)
-- ============================================================
INSERT INTO prestamos.puc (codigo, nombre, tipo, naturaleza, nivel) VALUES
  ('1', 'ACTIVO',                  'ACTIVO',           'DEBITO',  1),
  ('2', 'PASIVO',                  'PASIVO',            'CREDITO', 1),
  ('3', 'PATRIMONIO',              'PATRIMONIO',        'CREDITO', 1),
  ('4', 'INGRESOS',                'INGRESO',           'CREDITO', 1),
  ('5', 'GASTOS',                  'GASTO',             'DEBITO',  1),
  ('6', 'COSTOS DE VENTAS',        'COSTO_VENTA',       'DEBITO',  1),
  ('7', 'COSTOS DE PRODUCCION',    'COSTO_PRODUCCION',  'DEBITO',  1),
  ('8', 'ORDEN DEUDORA',           'ORDEN_DEUDORA',     'DEBITO',  1),
  ('9', 'ORDEN ACREEDORA',         'ORDEN_ACREEDORA',   'CREDITO', 1)
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- SEED — Grupos (nivel = 2)
-- ============================================================
INSERT INTO prestamos.puc (codigo, nombre, tipo, naturaleza, nivel) VALUES
  -- ACTIVO
  ('11', 'EFECTIVO Y EQUIVALENTES AL EFECTIVO',    'ACTIVO',  'DEBITO',  2),
  ('12', 'INVERSIONES',                            'ACTIVO',  'DEBITO',  2),
  ('13', 'DEUDORES',                               'ACTIVO',  'DEBITO',  2),
  ('14', 'INVENTARIOS',                            'ACTIVO',  'DEBITO',  2),
  ('15', 'PROPIEDADES PLANTA Y EQUIPO',            'ACTIVO',  'DEBITO',  2),
  ('16', 'INTANGIBLES',                            'ACTIVO',  'DEBITO',  2),
  ('17', 'DIFERIDOS',                              'ACTIVO',  'DEBITO',  2),
  ('19', 'OTROS ACTIVOS',                          'ACTIVO',  'DEBITO',  2),
  -- PASIVO
  ('21', 'OBLIGACIONES FINANCIERAS',               'PASIVO',  'CREDITO', 2),
  ('22', 'PROVEEDORES',                            'PASIVO',  'CREDITO', 2),
  ('23', 'CUENTAS POR PAGAR',                      'PASIVO',  'CREDITO', 2),
  ('24', 'IMPUESTOS GRAVAMENES Y TASAS',           'PASIVO',  'CREDITO', 2),
  ('25', 'OBLIGACIONES LABORALES',                 'PASIVO',  'CREDITO', 2),
  ('26', 'PASIVOS ESTIMADOS Y PROVISIONES',        'PASIVO',  'CREDITO', 2),
  ('27', 'DIFERIDOS PASIVO',                       'PASIVO',  'CREDITO', 2),
  ('28', 'OTROS PASIVOS',                          'PASIVO',  'CREDITO', 2),
  ('29', 'BONOS Y PAPELES COMERCIALES',            'PASIVO',  'CREDITO', 2),
  -- PATRIMONIO
  ('31', 'CAPITAL SOCIAL',                         'PATRIMONIO', 'CREDITO', 2),
  ('32', 'SUPERAVIT DE CAPITAL',                   'PATRIMONIO', 'CREDITO', 2),
  ('33', 'RESERVAS',                               'PATRIMONIO', 'CREDITO', 2),
  ('34', 'REVALORIZACION DEL PATRIMONIO',          'PATRIMONIO', 'CREDITO', 2),
  ('36', 'RESULTADOS DEL EJERCICIO',               'PATRIMONIO', 'CREDITO', 2),
  ('37', 'RESULTADOS DE EJERCICIOS ANTERIORES',    'PATRIMONIO', 'CREDITO', 2),
  -- INGRESOS
  ('41', 'INGRESOS OPERACIONALES',                 'INGRESO',  'CREDITO', 2),
  ('42', 'INGRESOS NO OPERACIONALES',              'INGRESO',  'CREDITO', 2),
  -- GASTOS
  ('51', 'GASTOS OPERACIONALES DE ADMINISTRACION', 'GASTO',    'DEBITO',  2),
  ('52', 'GASTOS OPERACIONALES DE VENTAS',         'GASTO',    'DEBITO',  2),
  ('53', 'GASTOS NO OPERACIONALES',                'GASTO',    'DEBITO',  2),
  ('54', 'IMPUESTO DE RENTA Y COMPLEMENTARIOS',    'GASTO',    'DEBITO',  2),
  -- COSTOS DE VENTAS
  ('61', 'COSTOS DE VENTAS',                       'COSTO_VENTA',      'DEBITO', 2),
  ('62', 'COMPRAS',                                'COSTO_VENTA',      'DEBITO', 2),
  -- COSTOS DE PRODUCCION
  ('71', 'MATERIALES',                             'COSTO_PRODUCCION', 'DEBITO', 2),
  ('72', 'MANO DE OBRA',                           'COSTO_PRODUCCION', 'DEBITO', 2),
  ('73', 'COSTOS INDIRECTOS DE FABRICACION',       'COSTO_PRODUCCION', 'DEBITO', 2),
  -- ORDEN DEUDORA
  ('81', 'DERECHOS CONTINGENTES',                  'ORDEN_DEUDORA',    'DEBITO',  2),
  ('83', 'ACREEDORAS DE CONTROL',                  'ORDEN_DEUDORA',    'DEBITO',  2),
  ('89', 'DEUDORAS FISCALES',                      'ORDEN_DEUDORA',    'DEBITO',  2),
  -- ORDEN ACREEDORA
  ('91', 'RESPONSABILIDADES CONTINGENTES',         'ORDEN_ACREEDORA',  'CREDITO', 2),
  ('93', 'DEUDORAS DE CONTROL',                    'ORDEN_ACREEDORA',  'CREDITO', 2),
  ('99', 'ACREEDORAS FISCALES',                    'ORDEN_ACREEDORA',  'CREDITO', 2)
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- SEED — Cuentas Mayor (nivel = 4)
-- ============================================================
INSERT INTO prestamos.puc (codigo, nombre, tipo, naturaleza, nivel) VALUES
  -- ACTIVO
  ('1105', 'CAJA',                                         'ACTIVO', 'DEBITO',  4),
  ('1110', 'DEPOSITOS EN INSTITUCIONES FINANCIERAS',       'ACTIVO', 'DEBITO',  4),
  ('1305', 'CLIENTES',                                     'ACTIVO', 'DEBITO',  4),
  ('1330', 'ANTICIPOS Y AVANCES',                         'ACTIVO', 'DEBITO',  4),
  ('1355', 'PRESTAMOS A PARTICULARES',                     'ACTIVO', 'DEBITO',  4),
  ('1360', 'CUENTAS POR COBRAR A SOCIOS Y ACCIONISTAS',   'ACTIVO', 'DEBITO',  4),
  ('1368', 'CUENTAS POR COBRAR A DIRECTORES',             'ACTIVO', 'DEBITO',  4),
  ('1380', 'DEUDORES VARIOS',                              'ACTIVO', 'DEBITO',  4),
  ('1399', 'PROVISIONES PARA DEUDORES',                    'ACTIVO', 'CREDITO', 4),
  -- PASIVO
  ('2105', 'BANCOS NACIONALES',                            'PASIVO', 'CREDITO', 4),
  ('2110', 'BANCOS DEL EXTERIOR',                          'PASIVO', 'CREDITO', 4),
  ('2305', 'PROVEEDORES NACIONALES',                       'PASIVO', 'CREDITO', 4),
  ('2335', 'COSTOS Y GASTOS POR PAGAR',                    'PASIVO', 'CREDITO', 4),
  ('2360', 'PRESTAMOS A SOCIOS Y ACCIONISTAS',             'PASIVO', 'CREDITO', 4),
  ('2365', 'RETENCION EN LA FUENTE',                       'PASIVO', 'CREDITO', 4),
  ('2367', 'IMPUESTO DE INDUSTRIA Y COMERCIO RETENIDO',    'PASIVO', 'CREDITO', 4),
  ('2368', 'IVA RETENIDO',                                 'PASIVO', 'CREDITO', 4),
  ('2380', 'ACREEDORES VARIOS',                            'PASIVO', 'CREDITO', 4),
  -- INGRESOS
  ('4210', 'INGRESOS FINANCIEROS',                         'INGRESO', 'CREDITO', 4),
  ('4295', 'INGRESOS DIVERSOS',                            'INGRESO', 'CREDITO', 4),
  -- GASTOS
  ('5305', 'GASTOS FINANCIEROS',                           'GASTO', 'DEBITO', 4),
  ('5310', 'PERDIDAS EN RETIRO DE ACTIVOS',                'GASTO', 'DEBITO', 4),
  ('5315', 'GASTOS EXTRAORDINARIOS',                       'GASTO', 'DEBITO', 4),
  ('5320', 'GASTOS DE EJERCICIOS ANTERIORES',              'GASTO', 'DEBITO', 4)
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- SEED — Subcuentas (nivel = 6) — préstamos intercompañía
-- ============================================================
INSERT INTO prestamos.puc (codigo, nombre, tipo, naturaleza, nivel) VALUES
  -- ACTIVO — Caja y Bancos
  ('110505', 'CAJA GENERAL',                                  'ACTIVO', 'DEBITO',  6),
  ('111005', 'BANCOS MONEDA NACIONAL',                        'ACTIVO', 'DEBITO',  6),
  ('111010', 'BANCOS MONEDA EXTRANJERA',                      'ACTIVO', 'DEBITO',  6),
  -- ACTIVO — Clientes
  ('130505', 'CLIENTES NACIONALES',                           'ACTIVO', 'DEBITO',  6),
  ('130510', 'CLIENTES DEL EXTERIOR',                         'ACTIVO', 'DEBITO',  6),
  -- ACTIVO — Préstamos intercompañía
  ('135505', 'PRESTAMOS INTERCOMPANIA POR COBRAR',            'ACTIVO', 'DEBITO',  6),
  ('136005', 'PRESTAMOS A SOCIOS PRINCIPALES',                'ACTIVO', 'DEBITO',  6),
  ('138005', 'DEUDORES VARIOS NACIONALES',                    'ACTIVO', 'DEBITO',  6),
  ('139905', 'PROVISION DEUDORES',                            'ACTIVO', 'CREDITO', 6),
  -- PASIVO — Obligaciones financieras
  ('210505', 'PRESTAMOS EN MONEDA NACIONAL',                  'PASIVO', 'CREDITO', 6),
  -- PASIVO — Cuentas por pagar
  ('233505', 'INTERESES POR PAGAR',                           'PASIVO', 'CREDITO', 6),
  ('233510', 'COMISIONES POR PAGAR',                          'PASIVO', 'CREDITO', 6),
  ('236005', 'PRESTAMOS INTERCOMPANIA POR PAGAR',             'PASIVO', 'CREDITO', 6),
  ('236505', 'RETENCION EN LA FUENTE POR PAGAR',              'PASIVO', 'CREDITO', 6),
  ('238005', 'ACREEDORES VARIOS NACIONALES',                  'PASIVO', 'CREDITO', 6),
  -- INGRESOS — Financieros
  ('421005', 'INTERESES PRESTAMOS',                           'INGRESO', 'CREDITO', 6),
  ('421010', 'INTERESES PRESTAMOS INTERCOMPANIA',             'INGRESO', 'CREDITO', 6),
  ('421015', 'DIVIDENDOS',                                    'INGRESO', 'CREDITO', 6),
  ('421020', 'DESCUENTOS FINANCIEROS',                        'INGRESO', 'CREDITO', 6),
  -- GASTOS — Financieros
  ('530505', 'GASTOS BANCARIOS',                              'GASTO', 'DEBITO', 6),
  ('530510', 'COMISIONES BANCARIAS',                          'GASTO', 'DEBITO', 6),
  ('530515', 'INTERESES POR PRESTAMOS',                       'GASTO', 'DEBITO', 6),
  ('530520', 'INTERESES PRESTAMOS INTERCOMPANIA',             'GASTO', 'DEBITO', 6),
  ('530525', 'GMF 4X1000',                                    'GASTO', 'DEBITO', 6),
  ('530530', 'DIFERENCIA EN CAMBIO',                          'GASTO', 'DEBITO', 6)
ON CONFLICT (codigo) DO NOTHING;
