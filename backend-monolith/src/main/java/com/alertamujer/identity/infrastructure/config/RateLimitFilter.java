package com.alertamujer.identity.infrastructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limita los intentos por IP y por minuto en los endpoints públicos de autenticación (fuerza bruta,
 * registro masivo, abuso del correo de recuperación). La IP es la del cliente real: detrás del proxy
 * de Render la resuelve Tomcat con X-Forwarded-For (server.forward-headers-strategy=native), sin
 * confiar en el valor que el propio cliente pueda inventar.
 */
@Component
public class RateLimitFilter implements Filter {

    static final int MAX_INTENTOS = 5;
    static final long VENTANA_MS = 60_000;
    private static final int MAX_ENTRADAS = 10_000;
    private static final Set<String> RUTAS = Set.of(
            "/api/auth/login", "/api/auth/google", "/api/auth/register",
            "/api/auth/forgot-password", "/api/auth/reset-password");

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ruta = httpRequest.getRequestURI();
        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && RUTAS.contains(ruta)) {
            long currentTime = System.currentTimeMillis();
            if (requestCounts.size() > MAX_ENTRADAS) {
                requestCounts.values().removeIf(c -> currentTime - c.timestamp > VENTANA_MS);
            }
            RequestCounter counter = requestCounts.computeIfAbsent(httpRequest.getRemoteAddr() + "|" + ruta,
                    k -> new RequestCounter(currentTime, new AtomicInteger(0)));

            synchronized (counter) {
                if (currentTime - counter.timestamp > VENTANA_MS) {
                    counter.timestamp = currentTime;
                    counter.count.set(0);
                }

                if (counter.count.incrementAndGet() > MAX_INTENTOS) {
                    httpResponse.setStatus(429); // Too Many Requests
                    httpResponse.setHeader("Retry-After", String.valueOf(VENTANA_MS / 1000));
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() +
                        "\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera un minuto e inténtalo de nuevo.\"}");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private static class RequestCounter {
        long timestamp;
        AtomicInteger count;

        public RequestCounter(long timestamp, AtomicInteger count) {
            this.timestamp = timestamp;
            this.count = count;
        }
    }
}
