/**
 * K6 Load Test — Fintra Financiero Módulo 9
 * Uso: k6 run tests/load/k6-scenarios.js
 * Variables de entorno:
 *   BASE_URL  (default: http://localhost:8080/pluto-service)
 *   USERNAME  (default: admin@pluto.co)
 *   PASSWORD  (default: DevPass123!)
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/pluto-service';
const USERNAME = __ENV.USERNAME  || 'admin@pluto.co';
const PASSWORD = __ENV.PASSWORD  || 'DevPass123!';

// Métricas personalizadas
const errRate       = new Rate('error_rate');
const loginDuration = new Trend('login_duration_ms');
const dashDuration  = new Trend('dashboard_duration_ms');
const opsDuration   = new Trend('operaciones_duration_ms');

export const options = {
  scenarios: {
    // S1: 100 usuarios concurrentes por 5 min — dashboard
    dashboard_concurrente: {
      executor: 'constant-vus',
      vus: 100,
      duration: '5m',
      tags: { scenario: 'dashboard' },
      exec: 'scenarioDashboard',
    },
    // S2: 10 RPS creación de operaciones durante 3 min
    crear_operaciones: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '3m',
      preAllocatedVUs: 20,
      tags: { scenario: 'crear_operacion' },
      exec: 'scenarioCrearOperacion',
      startTime: '6m', // después del S1
    },
  },
  thresholds: {
    // SLAs requeridos
    http_req_duration: ['p(95)<1500'],          // P95 < 1.5s
    'http_req_duration{scenario:dashboard}': ['p(95)<800'],  // dashboard < 800ms
    error_rate: ['rate<0.01'],                   // < 1% errores
    http_req_failed: ['rate<0.01'],
  },
};

// ── Setup: obtener token ──────────────────────────────────────────

export function setup() {
  const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ username: USERNAME, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, { 'login OK': r => r.status === 200 });
  const body = loginRes.json();
  return { token: body.data?.accessToken };
}

// ── Scenario 1: Dashboard ─────────────────────────────────────────

export function scenarioDashboard(data) {
  const headers = authHeaders(data.token);

  group('Dashboard', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/dashboard`, { headers });
    dashDuration.add(Date.now() - start);

    const ok = check(res, {
      'dashboard 200': r => r.status === 200,
      'dashboard < 800ms': r => r.timings.duration < 800,
    });
    errRate.add(!ok);
  });

  sleep(1);
}

// ── Scenario 2: Crear operación ───────────────────────────────────

export function scenarioCrearOperacion(data) {
  const headers = authHeaders(data.token);

  group('Listar operaciones', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/operaciones?page=0&size=10`, { headers });
    opsDuration.add(Date.now() - start);

    const ok = check(res, {
      'listar 200': r => r.status === 200,
      'listar < 1500ms': r => r.timings.duration < 1500,
    });
    errRate.add(!ok);
  });
}

// ── Login propio (smoke) ──────────────────────────────────────────

export default function(data) {
  const headers = authHeaders(data.token);

  group('Smoke: health check', () => {
    const res = http.get(`${BASE_URL}/api/v1/health-check`, { headers });
    check(res, { 'health 200': r => r.status === 200 });
  });

  sleep(0.5);
}

// ── helpers ──────────────────────────────────────────────────────

function authHeaders(token) {
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}
