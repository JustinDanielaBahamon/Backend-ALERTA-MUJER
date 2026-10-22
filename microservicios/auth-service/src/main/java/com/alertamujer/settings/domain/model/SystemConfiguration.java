package com.alertamujer.settings.domain.model;

import java.time.LocalDateTime;

/**
 * Configuración global de la app (una sola fila, singleton_key = 'SYSTEM'): la editan las
 * administradoras y la leen los demás módulos. Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class SystemConfiguration {

    private Long id;
    private String singletonKey = "SYSTEM";
    /** Minutos tras los cuales una alerta activa se marca como expirada. */
    private Integer sosMaxDurationMinutes;
    private String emergencyPhoneNumber;
    /** Correo de soporte que ven las usuarias. */
    private String adminEmail;
    private Boolean notificationsEnabled = true;
    private String defaultNotificationPriority = "normal";
    private String defaultTheme = "light";
    private Integer maxEmergencyContacts = 5;
    /** Versión mínima de la app móvil (p. ej. "1.2.0"); null si no se exige ninguna. */
    private String minAppVersion;
    private Integer updatedByAdminId;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** Lo pone un trigger de la base. */
    private LocalDateTime updatedAt;

    public static final String KEY = "SYSTEM";
    public static final int MIN_DURATION = 30;
    public static final int MAX_DURATION = 1440;
    public static final int MAX_CONTACTS = 20;

    // ── Reglas ──────────────────────────────────────────────────────

    /** Aplica los cambios enviados (null = sin cambio) validando cada valor; minAppVersion "" la quita. */
    public void update(SettingsChanges c, Long adminUserId, LocalDateTime now) {
        if (c.sosMaxDurationMinutes() != null) {
            int minutes = c.sosMaxDurationMinutes();
            if (minutes < MIN_DURATION || minutes > MAX_DURATION) {
                throw new IllegalArgumentException("La duración de una alerta debe estar entre "
                        + MIN_DURATION + " y " + MAX_DURATION + " minutos");
            }
            sosMaxDurationMinutes = minutes;
        }
        if (c.emergencyPhoneNumber() != null) {
            String number = c.emergencyPhoneNumber().trim();
            if (!number.matches("\\+?\\d{3,15}")) throw new IllegalArgumentException("Número de emergencia no válido");
            emergencyPhoneNumber = number;
        }
        if (c.supportEmail() != null) {
            String email = c.supportEmail().trim();
            if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("Correo de soporte no válido");
            adminEmail = email;
        }
        if (c.maxEmergencyContacts() != null) {
            int max = c.maxEmergencyContacts();
            if (max < 1 || max > MAX_CONTACTS) {
                throw new IllegalArgumentException("El límite de contactos debe estar entre 1 y " + MAX_CONTACTS);
            }
            maxEmergencyContacts = max;
        }
        if (c.minAppVersion() != null) {
            String version = c.minAppVersion().trim();
            if (!version.isEmpty() && !version.matches("\\d+(\\.\\d+){0,2}")) {
                throw new IllegalArgumentException("Versión no válida: usa el formato 1.2.0");
            }
            minAppVersion = version.isEmpty() ? null : version;
        }
        updatedByAdminId = adminUserId == null ? null : adminUserId.intValue();
        // El trigger de la base pone la misma hora; así se ve sin volver a leer la fila
        updatedAt = now;
    }

    /** Cuándo cambió por última vez (o cuándo se creó si nunca se editó). */
    public LocalDateTime lastUpdate() {
        return updatedAt != null ? updatedAt : createdAt;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSingletonKey() { return singletonKey; }
    public void setSingletonKey(String singletonKey) { this.singletonKey = singletonKey; }
    public Integer getSosMaxDurationMinutes() { return sosMaxDurationMinutes; }
    public void setSosMaxDurationMinutes(Integer sosMaxDurationMinutes) { this.sosMaxDurationMinutes = sosMaxDurationMinutes; }
    public String getEmergencyPhoneNumber() { return emergencyPhoneNumber; }
    public void setEmergencyPhoneNumber(String emergencyPhoneNumber) { this.emergencyPhoneNumber = emergencyPhoneNumber; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public String getDefaultNotificationPriority() { return defaultNotificationPriority; }
    public void setDefaultNotificationPriority(String defaultNotificationPriority) { this.defaultNotificationPriority = defaultNotificationPriority; }
    public String getDefaultTheme() { return defaultTheme; }
    public void setDefaultTheme(String defaultTheme) { this.defaultTheme = defaultTheme; }
    public Integer getMaxEmergencyContacts() { return maxEmergencyContacts; }
    public void setMaxEmergencyContacts(Integer maxEmergencyContacts) { this.maxEmergencyContacts = maxEmergencyContacts; }
    public String getMinAppVersion() { return minAppVersion; }
    public void setMinAppVersion(String minAppVersion) { this.minAppVersion = minAppVersion; }
    public Integer getUpdatedByAdminId() { return updatedByAdminId; }
    public void setUpdatedByAdminId(Integer updatedByAdminId) { this.updatedByAdminId = updatedByAdminId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
