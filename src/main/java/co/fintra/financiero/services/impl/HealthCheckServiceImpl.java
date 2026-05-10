package co.fintra.financiero.services.impl;

import co.fintra.financiero.services.interfaces.IHealthCheckService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckServiceImpl implements IHealthCheckService {

  private final EntityManager entityManager;

  @Override
  public HealthStatus check() {
    String dbStatus;
    try {
      entityManager.createNativeQuery("SELECT 1").getSingleResult();
      dbStatus = "UP";
    } catch (Exception e) {
      log.error("DB health check falló", e);
      dbStatus = "DOWN";
    }
    return new HealthStatus("UP", Instant.now().toString(), dbStatus);
  }
}
