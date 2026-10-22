package com.alertamujer.settings.domain.model;

import java.time.LocalDateTime;

/**
 * Global app configuration (a single row, singleton_key = 'SYSTEM'): edited by the
 * admins and read by the other modules. Domain model: knows neither the database nor Spring.
 */
public class SystemConfiguration {

    private Long id;
    private String singletonKey = "SYSTEM";
    /** Minutes after which an active alert is marked as expired. */
    private Integer sosMaxDurationMinutes;
    private String emergencyPhoneNumber;
    /** Support email the users see. */
    private String adminEmail;
    private Boolean notificationsEnabled = true;
    private String defaultNotificationPriority = "normal";
    private String defaultTheme = "light";
    private Integer maxEmergencyContacts = 5;
    /** Minimum mobile app version (e.g. "1.2.0"); null if none is required. */
    private String minAppVersion;
    private Integer updatedByAdminId;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** Set by a database trigger. */
    private LocalDateTime updatedAt;

    public static final String KEY = "SYSTEM";
    public static final int MIN_DURATION = 30;
    public static final int MAX_DURATION = 1440;
    public static final int MAX_CONTACTS = 20;

    // ── Reglas ──────────────────────────────────────────────────────

    /** Applies the sent changes (null = no change) validating each value; minAppVersion "" removes it. */
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
        // The database trigger sets the same time; this way it shows without reading the row again
        updatedAt = now;
    }

    /** When it last changed (or when it was created if never edited). */
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
