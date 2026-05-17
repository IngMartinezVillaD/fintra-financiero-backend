package co.pluto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integraciones.thomas-signe")
@Data
public class ThomasSigneProperties {
  private String baseUrl;
  private String apiKey;
  private String webhookSecret;
  private int timeoutMs = 10000;
  private int expiracionFirmaDias = 15;
  private String storagePath = "/tmp/pluto-docs";
}
