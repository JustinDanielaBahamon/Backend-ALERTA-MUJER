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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Solo aplicamos rate limiting al endpoint de login para proteger fuerza bruta
        if (httpRequest.getRequestURI().equals("/api/auth/login") && "POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String clientIp = httpRequest.getRemoteAddr();
            long currentTime = System.currentTimeMillis();

            requestCounts.putIfAbsent(clientIp, new RequestCounter(currentTime, new AtomicInteger(0)));
            RequestCounter counter = requestCounts.get(clientIp);

            // Reiniciar contador cada 60 segundos
            synchronized (counter) {
                if (currentTime - counter.timestamp > 60000) {
                    counter.timestamp = currentTime;
                    counter.count.set(0);
                }

                if (counter.count.incrementAndGet() > 5) { // Límite: 5 intentos por minuto
                    httpResponse.setStatus(429); // Too Many Requests
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + 
                        "\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos de inicio de sesión. Intente más tarde.\"}");
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