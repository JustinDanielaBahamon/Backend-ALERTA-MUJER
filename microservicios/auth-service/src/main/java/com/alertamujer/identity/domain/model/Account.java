package com.alertamujer.identity.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Credenciales de una persona (una cuenta por usuaria): hash de la contraseña y estado.
 */
public class Account {

    private Long id;
    private Long userId;
    private String passwordHash;
    /** active | inactive | blocked | pending */
    private String status = "active";
    private LocalDateTime lastAccess;

    public static final Set<String> STATUSES = Set.of("active", "inactive", "blocked", "pending");
    public static final int MIN_PASSWORD = 8;
    public static final int MAX_PASSWORD = 100;

    // ── Reglas ──────────────────────────────────────────────────────

    public static Account newOne(Long userId, String passwordHash) {
        Account a = new Account();
        a.userId = userId;
        a.passwordHash = passwordHash;
        return a;
    }

    /** Longitud permitida para una contraseña nueva. */
    public static void validateNewPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }

    public boolean blocked() {
        return "blocked".equals(status);
    }

    /** Una cuenta bloqueada o inactiva no puede seguir renovando su sesión. */
    public boolean canRefreshSession() {
        return !blocked() && !"inactive".equals(status);
    }

    public void recordAccess(LocalDateTime now) {
        lastAccess = now;
    }

    public void changePassword(String newHash) {
        passwordHash = newHash;
    }

    public void changeStatus(String newStatus) {
        if (!STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid account status. Must be one of: active, inactive, blocked, pending");
        }
        status = newStatus;
    }

    /** Cuenta eliminada: inactiva y con una contraseña que nadie conoce. */
    public void deactivate(String unusableHash) {
        status = "inactive";
        passwordHash = unusableHash;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastAccess() { return lastAccess; }
    public void setLastAccess(LocalDateTime lastAccess) { this.lastAccess = lastAccess; }
}
