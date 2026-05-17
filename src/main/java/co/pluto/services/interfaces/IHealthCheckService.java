package co.pluto.services.interfaces;

public interface IHealthCheckService {
  record HealthStatus(String status, String timestamp, String db) {}
  HealthStatus check();
}
