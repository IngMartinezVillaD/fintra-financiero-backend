package co.pluto.config.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter para endpoints de autenticación: máximo 10 intentos por IP cada 60 segundos.
 * Protege contra ataques de fuerza bruta en /auth/login y /auth/refresh.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final int    MAX_REQUESTS  = 10;
  private static final String RATE_LIMIT_RESPONSE =
      "{\"status\":\"error\",\"code\":429,\"message\":\"Demasiados intentos. Espere 60 segundos.\"}";

  private final Cache<String, AtomicInteger> requestCounts = Caffeine.newBuilder()
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .maximumSize(10_000)
      .build();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain chain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (!path.contains("/auth/login") && !path.contains("/auth/refresh")
        && !path.contains("/webhooks")) {
      chain.doFilter(request, response);
      return;
    }

    String key = getClientIp(request) + ":" + path;
    AtomicInteger count = requestCounts.get(key, k -> new AtomicInteger(0));

    if (count.incrementAndGet() > MAX_REQUESTS) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader("Retry-After", "60");
      response.getWriter().write(RATE_LIMIT_RESPONSE);
      return;
    }

    chain.doFilter(request, response);
  }

  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
