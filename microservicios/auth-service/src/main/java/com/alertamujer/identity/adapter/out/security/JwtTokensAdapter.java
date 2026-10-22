package com.alertamujer.identity.adapter.out.security;

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

    private static final long ACCESS_DURATION_MS = 24L * 60 * 60 * 1000;          // 24 horas
    private static final long REFRESH_DURATION_MS = 15L * 24 * 60 * 60 * 1000; // 15 días

    private final SecretKey key;

    JwtTokensAdapter(JwtKeyProvider jwtKeyProvider) {
        this.key = jwtKeyProvider.key();
    }

    @Override
    public String access(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_DURATION_MS))
                .signWith(key)
                .compact();
    }

    @Override
    public String refresh(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_DURATION_MS))
                .signWith(key)
                .compact();
    }

    @Override
    public Long userForRefresh(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("No es un token de renovación");
        }
        return Long.parseLong(claims.getSubject());
    }
}
