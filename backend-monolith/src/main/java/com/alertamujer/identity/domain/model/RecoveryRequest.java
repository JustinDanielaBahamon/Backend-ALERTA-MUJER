package com.alertamujer.identity.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Solicitud de recuperación de contraseña. Solo se guarda el hash del token, nunca el token.
 */
@Entity
@Table(name = "recovery_request", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RecoveryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "token_hash", nullable = false)
    @JsonIgnore
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "status", nullable = false)
    private String status = "pending";

    @Column(name = "attempts", nullable = false)
    private Long attempts = 0L;

    @Column(name = "requested_ip")
    private String requestedIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
