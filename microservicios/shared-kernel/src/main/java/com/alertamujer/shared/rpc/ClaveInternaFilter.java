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
 * Las rutas /internal/** solo las pueden llamar los otros microservicios: exigen la clave compartida
 * INTERNAL_API_KEY. Sin clave configurada se rechazan todas (nunca quedan abiertas por descuido).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClaveInternaFilter extends OncePerRequestFilter {

    private final byte[] clave;

    public ClaveInternaFilter(@Value("${internal.api-key:}") String clave) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibida = request.getHeader(ClaveInterna.CABECERA);
        boolean valida = clave.length > 0 && recibida != null
                && MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8));
        if (!valida) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para microservicios");
            return;
        }
        chain.doFilter(request, response);
    }
}
