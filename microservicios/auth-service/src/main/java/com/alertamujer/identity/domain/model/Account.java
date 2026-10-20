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

    public static final Set<String> ESTADOS = Set.of("active", "inactive", "blocked", "pending");
    public static final int MINIMO_CONTRASENA = 8;
    public static final int MAXIMO_CONTRASENA = 100;

    // ── Reglas ──────────────────────────────────────────────────────

    public static Account nueva(Long userId, String passwordHash) {
        Account a = new Account();
        a.userId = userId;
        a.passwordHash = passwordHash;
        return a;
    }

    /** Longitud permitida para una contraseña nueva. */
    public static void validarContrasenaNueva(String contrasena) {
        if (contrasena == null || contrasena.length() < MINIMO_CONTRASENA || contrasena.length() > MAXIMO_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }

    public boolean bloqueada() {
        return "blocked".equals(status);
    }

    /** Una cuenta bloqueada o inactiva no puede seguir renovando su sesión. */
    public boolean puedeRenovarSesion() {
        return !bloqueada() && !"inactive".equals(status);
    }

    public void registrarAcceso(LocalDateTime ahora) {
        lastAccess = ahora;
    }

    public void cambiarContrasena(String nuevoHash) {
        passwordHash = nuevoHash;
    }

    public void cambiarEstado(String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Invalid account status. Must be one of: active, inactive, blocked, pending");
        }
        status = estado;
    }

    /** Cuenta eliminada: inactiva y con una contraseña que nadie conoce. */
    public void desactivar(String hashInutilizable) {
        status = "inactive";
        passwordHash = hashInutilizable;
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
