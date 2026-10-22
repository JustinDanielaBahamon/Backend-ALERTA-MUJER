package com.alertamujer.identity.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Solicitud de recuperación de contraseña con un código de 6 dígitos. Solo se guarda el hash del
 * código. Vale 15 minutos y admite 5 intentos; pedir uno nuevo anula el anterior.
 */
public class RecoveryRequest {

    private Long id;
    private Long accountId;
    private String method;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    /** pending | replaced | expired | used */
    private String status = PENDING;
    private Long attempts = 0L;
    private String requestedIp;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final String PENDING = "pending";
    public static final int VALIDITY_MINUTES = 15;
    public static final int MAX_ATTEMPTS = 5;
    public static final int SECONDS_BETWEEN_REQUESTS = 60;

    // ── Reglas ──────────────────────────────────────────────────────

    public static RecoveryRequest newOne(Long accountId, String code, String ip, LocalDateTime now) {
        RecoveryRequest r = new RecoveryRequest();
        r.accountId = accountId;
        r.method = "email";
        r.tokenHash = hash(accountId, code);
        r.expiresAt = now.plusMinutes(VALIDITY_MINUTES);
        r.requestedIp = ip;
        r.createdAt = now;
        return r;
    }

    /** Hash del código ligado a la cuenta: el mismo código en otra cuenta da otro hash. */
    public static String hash(Long accountId, String code) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest((accountId + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Pedida hace menos de un minuto: evita que se usen los correos para molestar. */
    public boolean recent(LocalDateTime now) {
        return createdAt != null && createdAt.isAfter(now.minusSeconds(SECONDS_BETWEEN_REQUESTS));
    }

    public boolean expired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    /** Compara en tiempo constante. */
    public boolean coincide(String code) {
        return MessageDigest.isEqual(hash(accountId, code).getBytes(StandardCharsets.UTF_8),
                tokenHash.getBytes(StandardCharsets.UTF_8));
    }

    public void replace() {
        status = "replaced";
    }

    public void expire() {
        status = "expired";
    }

    /** Un código equivocado gasta un intento; al quinto el código deja de servir. */
    public void recordFailure() {
        attempts = attempts + 1;
        if (attempts >= MAX_ATTEMPTS) status = "expired";
    }

    public void use(LocalDateTime now) {
        status = "used";
        usedAt = now;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAttempts() { return attempts; }
    public void setAttempts(Long attempts) { this.attempts = attempts; }
    public String getRequestedIp() { return requestedIp; }
    public void setRequestedIp(String requestedIp) { this.requestedIp = requestedIp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
