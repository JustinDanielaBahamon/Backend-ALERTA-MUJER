package com.alertamujer.notifications.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Public link to follow an alert from the email or the app. The code has 256 random
 * bits and only travels to the contact; its hash is stored here. Valid while the alert is active and
 * up to 2 hours after it ends. Domain model: knows neither the database nor Spring.
 */
public class TrackingLink {

    private Long id;
    private Long alertId;
    private String tokenHash;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime revokedAt;

    public static final Duration VALIDITY_AFTER_END = Duration.ofHours(2);
    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Reglas ──────────────────────────────────────────────────────

    /** New 256-bit code in URL-safe Base64 (43 characters). It cannot be recovered later. */
    public static String generateCode() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static TrackingLink forAlert(Long alertId, String code) {
        TrackingLink l = new TrackingLink();
        l.alertId = alertId;
        l.tokenHash = hash(code);
        return l;
    }

    public static String hash(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Discards upfront codes that cannot be valid, without querying the database. */
    public static boolean possibleFormat(String code) {
        return code != null && code.length() >= 20 && code.length() <= 100;
    }

    /** The alert tracking is still available: active, or ended less than 2 hours ago. */
    public static boolean liveTracking(String alertStatus, LocalDateTime end, LocalDateTime now) {
        if ("active".equals(alertStatus)) return true;
        return end == null || !end.plus(VALIDITY_AFTER_END).isBefore(now);
    }

    public boolean revoked() {
        return revokedAt != null;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}
