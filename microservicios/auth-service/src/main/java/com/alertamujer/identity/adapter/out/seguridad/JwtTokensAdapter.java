package com.alertamujer.identity.adapter.out.seguridad;

import com.alertamujer.identity.application.port.out.TokensPort;
import com.alertamujer.shared.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Adaptador de salida: JWT firmados con JWT_SECRET. Alert y Evidence los validan con la misma clave
 * sin llamar a Auth Service.
 */
@Component
class JwtTokensAdapter implements TokensPort {

    private static final long DURACION_ACCESO_MS = 24L * 60 * 60 * 1000;          // 24 horas
    private static final long DURACION_RENOVACION_MS = 15L * 24 * 60 * 60 * 1000; // 15 días

    private final SecretKey clave;

    JwtTokensAdapter(JwtKeyProvider jwtKeyProvider) {
        this.clave = jwtKeyProvider.key();
    }

    @Override
    public String acceso(Long userId, String rol) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + DURACION_ACCESO_MS))
                .signWith(clave)
                .compact();
    }

    @Override
    public String renovacion(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + DURACION_RENOVACION_MS))
                .signWith(clave)
                .compact();
    }

    @Override
    public Long usuariaDeRenovacion(String token) {
        Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("No es un token de renovación");
        }
        return Long.parseLong(claims.getSubject());
    }
}
