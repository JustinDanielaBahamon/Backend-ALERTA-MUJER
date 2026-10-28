package com.alertamujer.alerts.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * A phone with automatic protection turned on. It gets its own key so it can start and end the
 * protection by itself, days after the app was last opened (when her session may have expired).
 * The key only serves for that: it reads nothing. Only its SHA-256 hash is stored.
 */
public class ProtectionDevice {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Long id;
    private Long userProfileId;
    private Long userId;
    private Long deviceId;
    private String keyHash;
    private LocalDateTime createdAt = LocalDateTime.now();

    /** The new device and its key, which is shown to the phone only once. */
    public record Enrolled(ProtectionDevice device, String key) {}

    public static Enrolled enroll(Long profile, Long userId, Long deviceId) {
        if (deviceId == null) throw new IllegalArgumentException("Falta el celular que activa la protección");
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        ProtectionDevice d = new ProtectionDevice();
        d.userProfileId = profile;
        d.userId = userId;
        d.deviceId = deviceId;
        d.keyHash = hash(key);
        return new Enrolled(d, key);
    }

    public static String hash(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
