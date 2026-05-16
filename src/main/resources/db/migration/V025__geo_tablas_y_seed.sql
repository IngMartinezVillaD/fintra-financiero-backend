-- ============================================================
-- V025 — Catálogo geográfico: países, departamentos, ciudades
-- Fuente códigos DANE: DIVIPOLA — dane.gov.co
-- Fuente códigos postales: 4-72 — codigopostal.gov.co
-- ============================================================

CREATE SCHEMA IF NOT EXISTS catalogos;

-- ── Países ────────────────────────────────────────────────────
CREATE TABLE catalogos.paises (
  id          SERIAL       PRIMARY KEY,
  codigo_iso2 CHAR(2)      NOT NULL,
  codigo_iso3 CHAR(3)      NOT NULL,
  nombre      VARCHAR(100) NOT NULL,
  activo      BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_paises_iso2 UNIQUE (codigo_iso2),
  CONSTRAINT uq_paises_iso3 UNIQUE (codigo_iso3)
);

COMMENT ON TABLE catalogos.paises IS 'Catálogo de países — ISO 3166-1';

-- ── Departamentos ─────────────────────────────────────────────
CREATE TABLE catalogos.departamentos (
  id          SERIAL       PRIMARY KEY,
  codigo_dane CHAR(2)      NOT NULL,
  nombre      VARCHAR(100) NOT NULL,
  pais_id     INTEGER      NOT NULL REFERENCES catalogos.paises(id),
  activo      BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_dept_codigo_pais UNIQUE (codigo_dane, pais_id)
);

COMMENT ON TABLE catalogos.departamentos IS 'División político-administrativa nivel 1 — códigos DIVIPOLA/DANE';

-- ── Ciudades / Municipios ─────────────────────────────────────
CREATE TABLE catalogos.ciudades (
  id               SERIAL       PRIMARY KEY,
  codigo_dane      CHAR(5)      NOT NULL,
  nombre           VARCHAR(120) NOT NULL,
  codigo_postal    CHAR(6),
  departamento_id  INTEGER      NOT NULL REFERENCES catalogos.departamentos(id),
  activo           BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_ciudades_codigo_dane UNIQUE (codigo_dane)
);

COMMENT ON TABLE catalogos.ciudades IS 'Municipios de Colombia — códigos DIVIPOLA y código postal 4-72';

-- ══════════════════════════════════════════════════════════════
-- SEED
-- ══════════════════════════════════════════════════════════════

-- ── País: Colombia ────────────────────────────────────────────
INSERT INTO catalogos.paises (codigo_iso2, codigo_iso3, nombre) VALUES ('CO', 'COL', 'Colombia');

-- ── Departamentos de Colombia ─────────────────────────────────
INSERT INTO catalogos.departamentos (codigo_dane, nombre, pais_id) VALUES
  ('91', 'Amazonas',                          (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('05', 'Antioquia',                         (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('81', 'Arauca',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('08', 'Atlántico',                         (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('11', 'Bogotá D.C.',                       (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('13', 'Bolívar',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('15', 'Boyacá',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('17', 'Caldas',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('18', 'Caquetá',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('85', 'Casanare',                          (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('19', 'Cauca',                             (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('20', 'Cesar',                             (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('27', 'Chocó',                             (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('23', 'Córdoba',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('25', 'Cundinamarca',                      (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('94', 'Guainía',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('95', 'Guaviare',                          (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('41', 'Huila',                             (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('44', 'La Guajira',                        (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('47', 'Magdalena',                         (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('50', 'Meta',                              (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('52', 'Nariño',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('54', 'Norte de Santander',                (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('86', 'Putumayo',                          (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('63', 'Quindío',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('66', 'Risaralda',                         (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('88', 'San Andrés y Providencia',          (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('68', 'Santander',                         (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('70', 'Sucre',                             (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('73', 'Tolima',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('76', 'Valle del Cauca',                   (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('97', 'Vaupés',                            (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO')),
  ('99', 'Vichada',                           (SELECT id FROM catalogos.paises WHERE codigo_iso2 = 'CO'));

-- ── Ciudades / Municipios ─────────────────────────────────────
-- Formato: (codigo_dane, nombre, codigo_postal, departamento_id)

-- Amazonas
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('91001', 'Leticia',       '910001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '91')),
  ('91540', 'Puerto Nariño', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '91'));

-- Antioquia
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('05001', 'Medellín',              '050010', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05088', 'Bello',                 '051010', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05129', 'Caldas',                null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05154', 'Caucasia',              null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05212', 'Copacabana',            '051010', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05266', 'Envigado',              '055420', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05360', 'Itagüí',                '055422', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05380', 'La Estrella',           null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05045', 'Apartadó',              '057800', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05615', 'Rionegro',              '054040', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05631', 'Sabaneta',              '055450', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05837', 'Turbo',                 null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05147', 'Carepa',                null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05172', 'Chigorodó',             null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05368', 'La Ceja',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05390', 'La Unión',              null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05467', 'Marinilla',             null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05107', 'Barbosa',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05197', 'El Carmen de Viboral',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05313', 'Girardota',             null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05604', 'Santa Fe de Antioquia', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05579', 'Puerto Berrío',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05736', 'Segovia',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05895', 'Yarumal',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05')),
  ('05400', 'La Pintada',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '05'));

-- Arauca
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('81001', 'Arauca',   '810001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '81')),
  ('81736', 'Saravena', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '81')),
  ('81794', 'Tame',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '81')),
  ('81065', 'Arauquita',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '81'));

-- Atlántico
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('08001', 'Barranquilla',  '080001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08758', 'Soledad',       '083030', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08433', 'Malambo',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08638', 'Sabanalarga',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08296', 'Galapa',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08078', 'Baranoa',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08')),
  ('08573', 'Puerto Colombia',null,    (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '08'));

-- Bogotá D.C.
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('11001', 'Bogotá D.C.', '110111', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '11'));

-- Bolívar
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('13001', 'Cartagena',             '130001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13430', 'Magangué',              null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13244', 'El Carmen de Bolívar',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13052', 'Arjona',                null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13760', 'Turbaco',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13440', 'Mompós',                null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13')),
  ('13490', 'San Juan Nepomuceno',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '13'));

-- Boyacá
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('15001', 'Tunja',         '150001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15244', 'Duitama',       '150460', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15762', 'Sogamoso',      '152210', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15176', 'Chiquinquirá',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15516', 'Paipa',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15693', 'Santa Rosa de Viterbo', null, (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15507', 'Nobsa',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15491', 'Moniquirá',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15')),
  ('15464', 'Villa de Leyva',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '15'));

-- Caldas
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('17001', 'Manizales',  '170001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17380', 'La Dorada',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17174', 'Chinchiná',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17662', 'Riosucio',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17777', 'Supía',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17042', 'Anserma',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17')),
  ('17867', 'Villamaría', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '17'));

-- Caquetá
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('18001', 'Florencia',            '180001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '18')),
  ('18610', 'San Vicente del Caguán', null,   (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '18')),
  ('18592', 'Puerto Rico',          null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '18')),
  ('18205', 'El Doncello',          null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '18'));

-- Casanare
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('85001', 'Yopal',      '850001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85')),
  ('85010', 'Aguazul',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85')),
  ('85440', 'Villanueva', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85')),
  ('85263', 'Monterrey',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85')),
  ('85400', 'Tauramena',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85')),
  ('85315', 'Paz de Ariporo', null, (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '85'));

-- Cauca
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('19001', 'Popayán',                  '190001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19')),
  ('19698', 'Santander de Quilichao',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19')),
  ('19573', 'Puerto Tejada',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19')),
  ('19807', 'Timbío',                   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19')),
  ('19548', 'Piendamó',                 null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19')),
  ('19397', 'La Sierra',                null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '19'));

-- Cesar
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('20001', 'Valledupar', '200001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20')),
  ('20011', 'Aguachica',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20')),
  ('20228', 'Codazzi',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20')),
  ('20013', 'Agustín Codazzi', null,(SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20')),
  ('20060', 'Bosconia',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20')),
  ('20238', 'Curumaní',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '20'));

-- Chocó
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('27001', 'Quibdó',   '270001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '27')),
  ('27361', 'Istmina',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '27')),
  ('27787', 'Tadó',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '27')),
  ('27660', 'Riosucio', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '27'));

-- Córdoba
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('23001', 'Montería',       '230001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23417', 'Lorica',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23660', 'Sahagún',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23162', 'Cereté',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23466', 'Montelíbano',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23555', 'Planeta Rica',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23')),
  ('23068', 'Ayapel',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '23'));

-- Cundinamarca
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('25754', 'Soacha',      '251650', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25175', 'Chía',        '250001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25473', 'Mosquera',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25430', 'Madrid',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25269', 'Facatativá',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25899', 'Zipaquirá',   '250251', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25290', 'Fusagasugá',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25307', 'Girardot',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25126', 'Cajicá',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25771', 'Sopó',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25785', 'Tabio',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25817', 'Tocancipá',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25183', 'Cota',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25')),
  ('25368', 'La Mesa',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '25'));

-- Guainía
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('94001', 'Inírida', '940001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '94'));

-- Guaviare
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('95001', 'San José del Guaviare', '950001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '95')),
  ('95025', 'El Retorno',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '95')),
  ('95015', 'Calamar',               null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '95'));

-- Huila
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('41001', 'Neiva',     '410001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41551', 'Pitalito',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41298', 'Garzón',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41396', 'La Plata',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41132', 'Campoalegre', null,   (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41530', 'Palermo',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41592', 'Rivera',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41')),
  ('41006', 'Acevedo',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '41'));

-- La Guajira
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('44001', 'Riohacha',          '440001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44')),
  ('44430', 'Maicao',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44')),
  ('44847', 'Uribia',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44')),
  ('44560', 'Manaure',           null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44')),
  ('44378', 'Fonseca',           null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44')),
  ('44420', 'San Juan del Cesar', null,    (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '44'));

-- Magdalena
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('47001', 'Santa Marta',  '470001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47')),
  ('47189', 'Ciénaga',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47')),
  ('47288', 'Fundación',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47')),
  ('47660', 'Plato',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47')),
  ('47245', 'El Banco',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47')),
  ('47053', 'Aracataca',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '47'));

-- Meta
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('50001', 'Villavicencio', '500001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50006', 'Acacías',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50313', 'Granada',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50226', 'El Dorado',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50450', 'Puerto López',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50568', 'Puerto Gaitán', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50685', 'Restrepo',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50')),
  ('50223', 'El Calvario',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '50'));

-- Nariño
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('52001', 'Pasto',    '520001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52835', 'Tumaco',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52356', 'Ipiales',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52786', 'Túquerres',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52418', 'La Unión', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52678', 'Samaniego',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52')),
  ('52079', 'Barbacoas',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '52'));

-- Norte de Santander
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('54001', 'Cúcuta',           '540001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54498', 'Ocaña',            null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54874', 'Villa del Rosario',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54405', 'Los Patios',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54174', 'Chinácota',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54518', 'Pamplona',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54800', 'Tibú',             null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54')),
  ('54261', 'El Zulia',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '54'));

-- Putumayo
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('86001', 'Mocoa',      '860001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '86')),
  ('86568', 'Puerto Asís',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '86')),
  ('86757', 'Orito',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '86')),
  ('86865', 'Valle del Guamuez', null, (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '86')),
  ('86755', 'Sibundoy',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '86'));

-- Quindío
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('63001', 'Armenia',    '630001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63')),
  ('63130', 'Calarcá',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63')),
  ('63470', 'Montenegro', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63')),
  ('63401', 'La Tebaida', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63')),
  ('63548', 'Quimbaya',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63')),
  ('63190', 'Circasia',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '63'));

-- Risaralda
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('66001', 'Pereira',            '660001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66')),
  ('66170', 'Dosquebradas',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66')),
  ('66682', 'Santa Rosa de Cabal',null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66')),
  ('66594', 'La Virginia',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66')),
  ('66075', 'Belén de Umbría',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66')),
  ('66615', 'Quinchía',           null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '66'));

-- San Andrés y Providencia
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('88001', 'San Andrés',  '880001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '88')),
  ('88564', 'Providencia', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '88'));

-- Santander
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('68001', 'Bucaramanga',   '680001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68276', 'Floridablanca', '681010', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68307', 'Girón',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68547', 'Piedecuesta',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68081', 'Barrancabermeja','687030',(SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68368', 'Lebrija',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68755', 'Socorro',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68679', 'San Gil',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68855', 'Vélez',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68')),
  ('68432', 'Málaga',        null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '68'));

-- Sucre
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('70001', 'Sincelejo',  '700001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70')),
  ('70215', 'Corozal',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70')),
  ('70708', 'San Marcos', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70')),
  ('70771', 'Sampués',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70')),
  ('70820', 'Tolú',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70')),
  ('70204', 'Morroa',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '70'));

-- Tolima
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('73001', 'Ibagué',    '730001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73268', 'Espinal',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73449', 'Melgar',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73349', 'Honda',     null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73411', 'Líbano',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73148', 'Chaparral', null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73')),
  ('73275', 'Flandes',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '73'));

-- Valle del Cauca
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('76001', 'Cali',          '760001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76520', 'Palmira',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76109', 'Buenaventura',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76834', 'Tuluá',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76111', 'Buga',          null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76147', 'Cartago',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76892', 'Yumbo',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76364', 'Jamundí',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76248', 'El Cerrito',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76275', 'Florida',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76376', 'La Unión',      null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76622', 'Roldanillo',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76736', 'Sevilla',       null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76845', 'Ulloa',         null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76036', 'Ansermanuevo',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76')),
  ('76130', 'Caicedonia',    null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '76'));

-- Vaupés
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('97001', 'Mitú', '970001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '97'));

-- Vichada
INSERT INTO catalogos.ciudades (codigo_dane, nombre, codigo_postal, departamento_id) VALUES
  ('99001', 'Puerto Carreño', '990001', (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '99')),
  ('99524', 'La Primavera',   null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '99')),
  ('99773', 'Santa Rosalía',  null,     (SELECT id FROM catalogos.departamentos WHERE codigo_dane = '99'));
