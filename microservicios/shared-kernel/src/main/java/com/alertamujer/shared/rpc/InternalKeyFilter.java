package com.alertamujer.shared.rpc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * /internal/** paths can only be called by the other microservices: they require the shared key
 * INTERNAL_API_KEY. Without a configured key all are rejected (never left open by mistake).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternalKeyFilter extends OncePerRequestFilter {

    private final byte[] key;

    public InternalKeyFilter(@Value("${internal.api-key:}") String key) {
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String received = request.getHeader(InternalKey.HEADER);
        boolean valid = key.length > 0 && received != null
                && MessageDigest.isEqual(key, received.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para microservicios");
            return;
        }
        chain.doFilter(request, response);
    }
}
