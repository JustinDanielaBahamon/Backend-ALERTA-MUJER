package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * "Avísame si no llego": un trayecto con hora de llegada. Si la usuaria no confirma a tiempo, el
 * servidor activa una alerta por ella. Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class SafeTrip {

    public static final String ACTIVO = "active";
    public static final String LLEGO = "arrived";
    public static final String CANCELADO = "cancelled";
    public static final String ALERTADO = "alerted";

    /** Límites de tiempo: evitan trayectos absurdos o temporizadores olvidados por días. */
    public static final int MINUTOS_MINIMOS = 5;
    public static final int MINUTOS_MAXIMOS = 240;
    public static final int EXTENSION_MAXIMA = 60;
    public static final int DURACION_TOTAL_MAXIMA = 360;
    /** Margen después de la hora de llegada antes de avisar (un semáforo, un ascensor...). */
    public static final Duration GRACIA = Duration.ofMinutes(2);
    /** Se le pregunta "¿ya llegaste?" este tiempo antes de la hora de llegada. */
    public static final Duration AVISO_PREVIO = Duration.ofMinutes(5);
    private static final int MAXIMO_DESTINO = 120;

    private Long id;
    private Long userProfileId;
    private Long userId;
    private Long deviceId;
    private String destination;
    private BigDecimal destinationLatitude;
    private BigDecimal destinationLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime expectedArrival;
    private String status = ACTIVO;
    private boolean reminderSent;
    private Long alertId;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    public static SafeTrip nuevo(Long perfil, Long userId, Long deviceId, String target, int minutos,
                                 BigDecimal lat, BigDecimal lng, LocalDateTime ahora) {
        if (target == null || target.isBlank()) throw new IllegalArgumentException("Escribe a dónde vas");
        if (minutos < MINUTOS_MINIMOS || minutos > MINUTOS_MAXIMOS) {
            throw new IllegalArgumentException("El tiempo de llegada debe estar entre " + MINUTOS_MINIMOS + " y "
                    + MINUTOS_MAXIMOS + " minutos");
        }
        if (deviceId == null) throw new IllegalArgumentException("Falta el celular desde el que sales");
        SafeTrip t = new SafeTrip();
        t.userProfileId = perfil;
        t.userId = userId;
        t.deviceId = deviceId;
        String limpio = target.trim();
        t.destination = limpio.length() > MAXIMO_DESTINO ? limpio.substring(0, MAXIMO_DESTINO) : limpio;
        t.destinationLatitude = lat;
        t.destinationLongitude = lng;
        t.startedAt = ahora;
        t.expectedArrival = ahora.plusMinutes(minutos);
        return t;
    }

    public boolean activo() {
        return ACTIVO.equals(status);
    }

    /** Pasó la hora de llegada (más el margen) sin que confirmara. */
    public boolean vencido(LocalDateTime ahora) {
        return activo() && ahora.isAfter(expectedArrival.plus(GRACIA));
    }

    /** Toca preguntarle si ya llegó (una sola vez). */
    public boolean porVencer(LocalDateTime ahora) {
        return activo() && !reminderSent && !ahora.isBefore(expectedArrival.minus(AVISO_PREVIO));
    }

    public void llegar(LocalDateTime ahora) {
        exigirActivo();
        status = LLEGO;
        endedAt = ahora;
    }

    public void cancelar(LocalDateTime ahora) {
        exigirActivo();
        status = CANCELADO;
        endedAt = ahora;
    }

    /** Más tiempo: hasta EXTENSION_MAXIMA cada vez y DURACION_TOTAL_MAXIMA en total. */
    public void extender(int minutos) {
        exigirActivo();
        if (minutos < 1 || minutos > EXTENSION_MAXIMA) {
            throw new IllegalArgumentException("Puedes agregar entre 1 y " + EXTENSION_MAXIMA + " minutos");
        }
        LocalDateTime nueva = expectedArrival.plusMinutes(minutos);
        if (Duration.between(startedAt, nueva).toMinutes() > DURACION_TOTAL_MAXIMA) {
            throw new IllegalArgumentException("Un trayecto no puede durar más de " + DURACION_TOTAL_MAXIMA / 60 + " horas");
        }
        expectedArrival = nueva;
        reminderSent = false; // se le vuelve a preguntar antes de la nueva hora
    }

    public void marcarRecordatorio() {
        reminderSent = true;
    }

    public void marcarAlertado(Long alert, LocalDateTime ahora) {
        status = ALERTADO;
        alertId = alert;
        endedAt = ahora;
    }

    public boolean esDe(Long perfil) {
        return perfil != null && perfil.equals(userProfileId);
    }

    private void exigirActivo() {
        if (!activo()) throw new IllegalArgumentException("Este trayecto ya terminó");
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
