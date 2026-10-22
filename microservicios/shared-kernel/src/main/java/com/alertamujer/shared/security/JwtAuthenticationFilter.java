package com.alertamujer.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey jwtKey;

    public JwtAuthenticationFilter(JwtKeyProvider jwtKeyProvider) {
        this.jwtKey = jwtKeyProvider.key();
    }

    /**
     * El seguimiento público (enlace del correo a los contactos) no usa sesión: si el navegador manda
     * un token viejo o vencido se ignora, para que un 401 nunca le cierre la página a quien ayuda.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
           try {
                // Lógica de lectura y parseo del JWT
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Long userId = Long.parseLong(claims.getSubject());
                String role = claims.get("role", String.class);

                List<GrantedAuthority> authorities = Collections.emptyList();
                if (role != null) {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // 1. Primero atrapamos la expiración específica
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + 
                    "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Access Token expirado\"}");
                return; 
            } catch (Exception e) {
                // 2. Luego atrapamos cualquier otro error (firma inválida, malformado, etc.)
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + 
                    "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token inválido o malformado\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
