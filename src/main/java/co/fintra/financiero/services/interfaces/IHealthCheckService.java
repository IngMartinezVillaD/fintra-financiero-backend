package co.fintra.financiero.services.interfaces;

public interface IHealthCheckService {
  record HealthStatus(String status, String timestamp, String db) {}
  HealthStatus check();
}
