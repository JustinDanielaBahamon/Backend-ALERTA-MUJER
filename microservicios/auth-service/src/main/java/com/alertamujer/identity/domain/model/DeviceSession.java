package com.alertamujer.identity.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Sesión de un dispositivo (refresh token). Solo se guarda el hash del refresh token.
 */
@Entity
@Table(name = "device_session", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "refresh_token_hash", nullable = false)
    @JsonIgnore
    private String refreshTokenHash;

    @Column(name = "status", nullable = false)
    private String status = "active";

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
