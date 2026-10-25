package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * "Avísame si no llego": a trip with an arrival time. If the user does not confirm in time, the
 * server activates an alert for her. During the trip the phone sends a signal of life with its
 * location; if it stops arriving (phone off, no signal, taken away) the server alerts before the
 * arrival time.
 *
 * Automatic protection (mode AUTO): her phone starts it by itself when she leaves one of her frequent
 * places. It has no destination nor arrival time: only the lost signal alerts, and when its window
 * ends it simply ends. Domain model: knows neither the database nor Spring.
 */
public class SafeTrip {

    public static final String ACTIVE = "active";
    public static final String ARRIVED = "arrived";
    public static final String CANCELLED = "cancelled";
    public static final String ALERTED = "alerted";
    /** An automatic protection that finished without anything happening. */
    public static final String ENDED = "ended";

    public static final String MODE_TRIP = "trip";
    public static final String MODE_AUTO = "auto";
    /** What her contacts read as the "destination" of an automatic protection. */
    public static final String AUTO_DESTINATION = "Protección automática";
    /** An automatic protection ends by itself after this long (she can be asked again on the next exit). */
    public static final Duration AUTO_WINDOW = Duration.ofHours(4);
    /** Without being asked, the wait for her signal is a bit longer than in a trip she started herself. */
    public static final Duration AUTO_SIGNAL_TIMEOUT = Duration.ofMinutes(20);

    /** Why the server activated the alert. */
    public static final String REASON_NOT_CONFIRMED = "not_confirmed";
    public static final String REASON_SIGNAL_LOST = "signal_lost";
    /** She stopped inside a risk zone and did not answer "¿Todo bien?" (automatic protection). */
    public static final String REASON_NO_ANSWER = "no_answer";

    /** Time limits: avoid absurd trips or timers forgotten for days. */
    public static final int MIN_MINUTES = 5;
    public static final int MAX_MINUTES = 240;
    public static final int MAX_EXTENSION = 60;
    public static final int MAX_TOTAL_DURATION = 360;
    /** Margin after the arrival time before notifying (a traffic light, an elevator...). */
    public static final Duration GRACE = Duration.ofMinutes(2);
    /** She is asked "¿ya llegaste?" this long before the arrival time. */
    public static final Duration REMINDER_LEAD = Duration.ofMinutes(5);
    /** Without a signal of life for this long the server alerts (shorter inside a high-risk zone). */
    public static final Duration SIGNAL_TIMEOUT = Duration.ofMinutes(15);
    public static final Duration SIGNAL_TIMEOUT_RISK_ZONE = Duration.ofMinutes(5);
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
    /** Last signal of life received (null: her phone does not send them, e.g. an old app version). */
    private LocalDateTime lastSeenAt;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;
    private String alertReason;
    private String mode = MODE_TRIP;
    /** Her phone reported a battery below 10 %: if the signal is lost, the contacts are told so. */
    private LocalDateTime lowBatteryAt;

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

    /** Automatic protection: starts when she leaves a frequent place, from her last location. */
    public static SafeTrip automatic(Long profile, Long userId, Long deviceId, BigDecimal lat, BigDecimal lng,
                                     LocalDateTime now) {
        if (deviceId == null) throw new IllegalArgumentException("Falta el celular desde el que sales");
        SafeTrip t = new SafeTrip();
        t.mode = MODE_AUTO;
        t.userProfileId = profile;
        t.userId = userId;
        t.deviceId = deviceId;
        t.destination = AUTO_DESTINATION;
        t.startedAt = now;
        t.expectedArrival = now.plus(AUTO_WINDOW);
        // Her phone is already sending signals: the lost signal counts from the start
        t.lastSeenAt = now;
        t.lastLatitude = lat;
        t.lastLongitude = lng;
        return t;
    }

    public boolean automatic() {
        return MODE_AUTO.equals(mode);
    }

    /** The window of an automatic protection passed: it ends without alerting. */
    public boolean windowOver(LocalDateTime now) {
        return automatic() && active() && !now.isBefore(expectedArrival);
    }

    /** Ends an automatic protection (she arrived at a frequent place, stopped somewhere or stopped it). */
    public void end(LocalDateTime now) {
        requireActive();
        status = ENDED;
        endedAt = now;
    }

    public boolean active() {
        return ACTIVE.equals(status);
    }

    /** The arrival time (plus the margin) passed without her confirming. */
    public boolean expired(LocalDateTime now) {
        return active() && !automatic() && now.isAfter(expectedArrival.plus(GRACE));
    }

    /** Time to ask her whether she arrived (only once). */
    public boolean dueSoon(LocalDateTime now) {
        return active() && !automatic() && !reminderSent && !now.isBefore(expectedArrival.minus(REMINDER_LEAD));
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

    /** More time: up to MAX_EXTENSION each time and MAX_TOTAL_DURATION in total. */
    public void extender(int minutes) {
        requireActive();
        if (automatic()) throw new IllegalArgumentException("La protección automática no tiene hora de llegada");
        if (minutes < 1 || minutes > MAX_EXTENSION) {
            throw new IllegalArgumentException("Puedes agregar entre 1 y " + MAX_EXTENSION + " minutos");
        }
        LocalDateTime newOne = expectedArrival.plusMinutes(minutes);
        if (Duration.between(startedAt, newOne).toMinutes() > MAX_TOTAL_DURATION) {
            throw new IllegalArgumentException("Un trayecto no puede durar más de " + MAX_TOTAL_DURATION / 60 + " horas");
        }
        expectedArrival = newOne;
        reminderSent = false; // she is asked again before the new time
    }

    /** Signal of life from her phone. Only counts while the trip is active. */
    public void heartbeat(BigDecimal lat, BigDecimal lng, LocalDateTime now) {
        heartbeat(lat, lng, false, now);
    }

    /** lowBattery: her phone is below 10 %, so a lost signal is explained to her contacts. */
    public void heartbeat(BigDecimal lat, BigDecimal lng, boolean lowBattery, LocalDateTime now) {
        requireActive();
        lastSeenAt = now;
        if (lowBattery && lowBatteryAt == null) lowBatteryAt = now;
        if (lat != null && lng != null) {
            lastLatitude = lat;
            lastLongitude = lng;
        }
    }

    /**
     * Her phone was sending signals of life and they stopped for longer than the timeout. Only before the
     * arrival time: after it, the trip is alerted as not confirmed. Without any signal (an app that does
     * not send them) it never applies, so it gives no false alarms.
     */
    public boolean signalLost(LocalDateTime now, Duration timeout) {
        return active() && lastSeenAt != null && now.isAfter(lastSeenAt.plus(timeout)) && !expired(now)
                && !windowOver(now);
    }

    public void markReminder() {
        reminderSent = true;
    }

    public void markAlerted(Long alert, LocalDateTime now, String reason) {
        status = ALERTED;
        alertId = alert;
        endedAt = now;
        alertReason = reason;
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
    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public BigDecimal getLastLatitude() { return lastLatitude; }
    public void setLastLatitude(BigDecimal v) { this.lastLatitude = v; }
    public BigDecimal getLastLongitude() { return lastLongitude; }
    public void setLastLongitude(BigDecimal v) { this.lastLongitude = v; }
    public String getAlertReason() { return alertReason; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public LocalDateTime getLowBatteryAt() { return lowBatteryAt; }
    public void setLowBatteryAt(LocalDateTime lowBatteryAt) { this.lowBatteryAt = lowBatteryAt; }
    public void setAlertReason(String alertReason) { this.alertReason = alertReason; }
}
