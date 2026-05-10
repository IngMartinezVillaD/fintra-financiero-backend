package co.fintra.financiero.services.impl;

import co.fintra.financiero.models.entity.AuditoriaLoginEntity;
import co.fintra.financiero.models.repositories.IAuditoriaLoginRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListenerImpl {

  private final IAuditoriaLoginRepository auditoriaLoginRepository;

  @Async
  @EventListener
  public void onSuccess(AuthenticationSuccessEvent event) {
    guardarEvento(event.getAuthentication().getName(), true);
  }

  @Async
  @EventListener
  public void onFailure(AbstractAuthenticationFailureEvent event) {
    log.warn("Login fallido para: {}", event.getAuthentication().getName());
    guardarEvento(event.getAuthentication().getName(), false);
  }

  private void guardarEvento(String username, boolean exitoso) {
    String ip = null;
    String userAgent = null;
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        HttpServletRequest req = attrs.getRequest();
        ip = req.getRemoteAddr();
        userAgent = req.getHeader("User-Agent");
      }
    } catch (Exception ignored) {}

    auditoriaLoginRepository.save(AuditoriaLoginEntity.builder()
        .username(username)
        .exitoso(exitoso)
        .ip(ip)
        .userAgent(userAgent)
        .build());
  }
}
