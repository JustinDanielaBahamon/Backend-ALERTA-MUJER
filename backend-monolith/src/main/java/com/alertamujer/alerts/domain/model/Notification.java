package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notification", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(nullable = false)
    private String type;

    private String priority = "normal";

    private String title;
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_persistent", nullable = false)
    private Boolean isPersistent = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
