package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * "Avísame si no llego": un trayecto con hora de llegada. Si la usuaria no confirma a tiempo, el
 * servidor activa una alerta por ella. Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class SafeTrip {

    public static final String ACTIVE = "active";
    public static final String ARRIVED = "arrived";
    public static final String CANCELLED = "cancelled";
    public static final String ALERTED = "alerted";

    /** Límites de tiempo: evitan trayectos absurdos o temporizadores olvidados por días. */
    public static final int MIN_MINUTES = 5;
    public static final int MAX_MINUTES = 240;
    public static final int EXTENSION_MAXIMA = 60;
    public static final int MAX_TOTAL_DURATION = 360;
    /** Margen después de la hora de llegada antes de avisar (un semáforo, un ascensor...). */
    public static final Duration GRACE = Duration.ofMinutes(2);
    /** Se le pregunta "¿ya llegaste?" este tiempo antes de la hora de llegada. */
    public static final Duration REMINDER_LEAD = Duration.ofMinutes(5);
    private static final int MAX_DESTINATION = 120;

    private Long id;
    private Long userProfileId;
    private Long userId;
    private Long deviceId;
    private String destination;
    private BigDecimal destinationLatitude;
    private BigDecimal destinationLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime expectedArrival;
    private String status = ACTIVE;
    private boolean reminderSent;
    private Long alertId;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    public static SafeTrip created(Long profile, Long userId, Long deviceId, String target, int minutes,
                                 BigDecimal lat, BigDecimal lng, LocalDateTime now) {
        if (target == null || target.isBlank()) throw new IllegalArgumentException("Escribe a dónde vas");
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException("El tiempo de llegada debe estar entre " + MIN_MINUTES + " y "
                    + MAX_MINUTES + " minutos");
        }
        if (deviceId == null) throw new IllegalArgumentException("Falta el celular desde el que sales");
        SafeTrip t = new SafeTrip();
        t.userProfileId = profile;
        t.userId = userId;
        t.deviceId = deviceId;
        String clean = target.trim();
        t.destination = clean.length() > MAX_DESTINATION ? clean.substring(0, MAX_DESTINATION) : clean;
        t.destinationLatitude = lat;
        t.destinationLongitude = lng;
        t.startedAt = now;
        t.expectedArrival = now.plusMinutes(minutes);
        return t;
    }

    public boolean active() {
        return ACTIVE.equals(status);
    }

    /** Pasó la hora de llegada (más el margen) sin que confirmara. */
    public boolean expired(LocalDateTime now) {
        return active() && now.isAfter(expectedArrival.plus(GRACE));
    }

    /** Toca preguntarle si ya llegó (una sola vez). */
    public boolean dueSoon(LocalDateTime now) {
        return active() && !reminderSent && !now.isBefore(expectedArrival.minus(REMINDER_LEAD));
    }

    public void arrive(LocalDateTime now) {
        requireActive();
        status = ARRIVED;
        endedAt = now;
    }

    public void cancel(LocalDateTime now) {
        requireActive();
        status = CANCELLED;
        endedAt = now;
    }

    /** Más tiempo: hasta EXTENSION_MAXIMA cada vez y DURACION_TOTAL_MAXIMA en total. */
    public void extender(int minutes) {
        requireActive();
        if (minutes < 1 || minutes > EXTENSION_MAXIMA) {
            throw new IllegalArgumentException("Puedes agregar entre 1 y " + EXTENSION_MAXIMA + " minutos");
        }
        LocalDateTime newOne = expectedArrival.plusMinutes(minutes);
        if (Duration.between(startedAt, newOne).toMinutes() > MAX_TOTAL_DURATION) {
            throw new IllegalArgumentException("Un trayecto no puede durar más de " + MAX_TOTAL_DURATION / 60 + " horas");
        }
        expectedArrival = newOne;
        reminderSent = false; // se le vuelve a preguntar antes de la nueva hora
    }

    public void markReminder() {
        reminderSent = true;
    }

    public void markAlerted(Long alert, LocalDateTime now) {
        status = ALERTED;
        alertId = alert;
        endedAt = now;
    }

    public boolean esDe(Long profile) {
        return profile != null && profile.equals(userProfileId);
    }

    private void requireActive() {
        if (!active()) throw new IllegalArgumentException("Este trayecto ya terminó");
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(BigDecimal v) { this.destinationLatitude = v; }
    public BigDecimal getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(BigDecimal v) { this.destinationLongitude = v; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getExpectedArrival() { return expectedArrival; }
    public void setExpectedArrival(LocalDateTime expectedArrival) { this.expectedArrival = expectedArrival; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
