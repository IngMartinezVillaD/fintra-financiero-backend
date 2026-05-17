package co.pluto.controllers;

import co.pluto.services.interfaces.IFirmaDigitalService;
import co.pluto.utils.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Endpoints públicos para integraciones externas")
public class WebhookController {

  private final IFirmaDigitalService firmaService;

  @PostMapping("/thomas-signe")
  @Operation(summary = "Webhook Thomas Signe — firma digital (sin JWT, validado por HMAC)")
  public ResponseEntity<Void> thomasSigne(
      @RequestHeader(value = "X-TS-Signature", required = false) String signature,
      @RequestBody String payload) {

    try {
      firmaService.procesarWebhook(payload, signature);
      return ResponseEntity.ok().build();
    } catch (CustomException e) {
      log.warn("Webhook Thomas Signe rechazado: {}", e.getMessage());
      return ResponseEntity.status(e.getStatus() != null
          ? e.getStatus().value() : HttpStatus.UNAUTHORIZED.value()).build();
    } catch (Exception e) {
      log.error("Error procesando webhook Thomas Signe", e);
      return ResponseEntity.ok().build(); // Siempre 200 para evitar reintentos infinitos
    }
  }
}
