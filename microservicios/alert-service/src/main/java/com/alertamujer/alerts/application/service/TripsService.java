package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.AutoProtectionUseCase;
import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.in.ReviewTripsUseCase;
import com.alertamujer.alerts.application.port.in.TripsUseCase;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.ProtectionDeviceRepository;
import com.alertamujer.alerts.application.port.out.PushPort;
import com.alertamujer.alerts.application.port.out.RiskZonesPort;
import com.alertamujer.alerts.application.port.out.TripRepository;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.domain.model.ProtectionDevice;
import com.alertamujer.alerts.domain.model.SafeTrip;
import com.alertamujer.shared.exception.ConflictException;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.ColombiaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * "Avísame si no llego". If the user does not confirm her arrival in time, a normal alert is activated
 * (email and push to her contacts) with her last known location: it works even if her phone has
 * no data or credit, because the server activates it. During the trip her phone sends signals of life:
 * if they stop (phone off, no signal, taken away) the alert goes out before the arrival time, sooner
 * inside a high-risk zone.
 *
 * Automatic protection reuses the same trip without destination: her phone starts it when she leaves a
 * frequent place (with its own key, see ProtectionDevice) and only a lost signal or an unanswered
 * "¿Todo bien?" inside a risk zone alerts.
 */
@Service
public class TripsService implements TripsUseCase, ReviewTripsUseCase, AutoProtectionUseCase {

    private static final Logger log = LoggerFactory.getLogger(TripsService.class);
    /** Prevents abuse (thousands of timers) without getting in the way of normal use. */
    static final int MAX_PER_DAY = 20;
    /** Spanish 12-hour time ("10:00 p. m."): the server locale is English and wrote "10:00 PM". */
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-CO"));

    private final TripRepository trips;
    private final IdentityPort identity;
    private final ManageOwnAlertsUseCase alerts;
    private final LocationRepository locations;
    private final PhonesPort phones;
    private final PushPort push;
    private final RiskZonesPort riskZones;
    private final ProtectionDeviceRepository protectedPhones;

    public TripsService(TripRepository trips, IdentityPort identity, ManageOwnAlertsUseCase alerts,
                            LocationRepository locations, PhonesPort phones, PushPort push, RiskZonesPort riskZones,
                            ProtectionDeviceRepository protectedPhones) {
        this.trips = trips;
        this.identity = identity;
        this.alerts = alerts;
        this.locations = locations;
        this.phones = phones;
        this.push = push;
        this.riskZones = riskZones;
        this.protectedPhones = protectedPhones;
    }

    @Override
    @Transactional
    public SafeTrip start(NewTrip created, Long userId) {
        Long profile = identity.requiredProfile(userId);
        LocalDateTime now = LocalDateTime.now();
        Optional<SafeTrip> current = trips.activeOf(profile);
        if (current.isPresent() && current.get().automatic()) {
            // A trip she starts herself (with a destination) replaces the automatic protection
            current.get().end(now);
            trips.save(current.get());
        } else if (current.isPresent()) {
            throw new ConflictException("Ya tienes un trayecto en curso: confirma tu llegada o cancélalo primero");
        }
        if (trips.startedSince(profile, now.minusHours(24)) >= MAX_PER_DAY) {
            throw new IllegalArgumentException("Llegaste al máximo de " + MAX_PER_DAY + " trayectos por día");
        }
        return trips.save(SafeTrip.created(profile, userId, created.deviceId(), created.target(), created.minutes(),
                created.latitude(), created.longitude(), now));
    }

    @Override
    public Optional<SafeTrip> active(Long userId) {
        return identity.profileOf(userId).flatMap(trips::activeOf);
    }

    @Override
    @Transactional
    public SafeTrip arrived(Long id, Long userId) {
        SafeTrip t = own(id, userId);
        t.arrive(LocalDateTime.now());
        return trips.save(t);
    }

    @Override
    @Transactional
    public SafeTrip extender(Long id, int minutes, Long userId) {
        SafeTrip t = own(id, userId);
        t.extender(minutes);
        return trips.save(t);
    }

    @Override
    @Transactional
    public SafeTrip cancel(Long id, Long userId) {
        SafeTrip t = own(id, userId);
        t.cancel(LocalDateTime.now());
        return trips.save(t);
    }

    @Override
    @Transactional
    public SafeTrip heartbeat(Long id, Heartbeat point, Long userId) {
        SafeTrip t = own(id, userId);
        LocalDateTime now = LocalDateTime.now();
        if (point.latitude() != null && point.longitude() != null) {
            LocationLog l = LocationLog.created(t.getUserProfileId(), point.latitude(), point.longitude(), point.accuracy());
            l.recordTime(point.recordedAt(), now);
            // After the server alerted, the route the phone kept offline belongs to that alert
            if (!t.active()) l.setAlertId(t.getAlertId());
            locations.save(l);
        }
        if (!t.active()) return t;
        t.heartbeat(point.latitude(), point.longitude(), now);
        return trips.save(t);
    }

    // ── Periodic review ──────────────────────────────────────────

    @Override
    @Transactional
    public int review(LocalDateTime now) {
        int alerted = 0;
        for (SafeTrip t : trips.allActive()) {
            try {
                if (t.windowOver(now)) {
                    t.end(now);
                    trips.save(t);
                } else if (t.expired(now)) {
                    raiseAlert(t, now, SafeTrip.REASON_NOT_CONFIRMED);
                    alerted++;
                } else if (t.signalLost(now, signalTimeout(t))) {
                    raiseAlert(t, now, SafeTrip.REASON_SIGNAL_LOST);
                    alerted++;
                } else if (t.dueSoon(now)) {
                    askIfArrived(t, now);
                }
            } catch (RuntimeException e) {
                // A trip with problems must not prevent reviewing the others
                log.error("No se pudo revisar el trayecto {}: {}", t.getId(), e.getMessage());
            }
        }
        return alerted;
    }

    /** Inside a high-risk zone the server waits less for her signal of life. */
    private Duration signalTimeout(SafeTrip t) {
        if (riskZones.isHighRisk(t.getLastLatitude(), t.getLastLongitude())) return SafeTrip.SIGNAL_TIMEOUT_RISK_ZONE;
        return t.automatic() ? SafeTrip.AUTO_SIGNAL_TIMEOUT : SafeTrip.SIGNAL_TIMEOUT;
    }

    private void raiseAlert(SafeTrip t, LocalDateTime now, String reason) {
        Optional<LocationLog> latest = locations.latest(t.getUserProfileId());
        boolean signalLost = SafeTrip.REASON_SIGNAL_LOST.equals(reason);
        String message = t.automatic() ? autoMessage(t, reason) : signalLost
                ? "Se perdió la señal de su celular durante el trayecto a " + t.getDestination()
                        + ". Última señal a las " + TIME.format(ColombiaTime.of(t.getLastSeenAt()))
                        + "; debía llegar a las " + TIME.format(ColombiaTime.of(t.getExpectedArrival())) + "."
                : "No confirmó que llegó a " + t.getDestination() + ". Salió a las "
                        + TIME.format(ColombiaTime.of(t.getStartedAt())) + " y debía llegar a las "
                        + TIME.format(ColombiaTime.of(t.getExpectedArrival())) + "."
                        + latest.map(l -> " Última ubicación hace " + minutesSince(l.getRecordedAt(), now) + " min.").orElse("");
        // The last signal of life is the freshest location; then the route; then the destination
        var lat = t.getLastLatitude() != null ? t.getLastLatitude()
                : latest.map(LocationLog::getLatitude).orElse(t.getDestinationLatitude());
        var lng = t.getLastLongitude() != null ? t.getLastLongitude()
                : latest.map(LocationLog::getLongitude).orElse(t.getDestinationLongitude());
        String method = t.automatic() ? (signalLost ? "signal_lost" : "auto") : signalLost ? "signal_lost" : "timer";
        Alert alert = alerts.activate(new NewAlert(t.getDeviceId(), "main", method,
                "active", message, lat, lng, null, null), t.getUserId());
        t.markAlerted(alert.getId(), now, reason);
        trips.save(t);
        tellHer(t, alert.getId(), reason);
        log.info("Trayecto {} ({}): alerta {} activada", t.getId(), reason, alert.getId());
    }

    /**
     * Her own phones learn that the server activated the alert for her: opening the app shows the
     * active alert screen right away (and keeps sending her location from there).
     */
    private void tellHer(SafeTrip t, Long alertId, String reason) {
        List<String> tokens = phones.phoneTokens(t.getUserId());
        if (tokens.isEmpty()) return;
        String why = SafeTrip.REASON_SIGNAL_LOST.equals(reason) ? "Tu celular dejó de enviar señal"
                : SafeTrip.REASON_NO_ANSWER.equals(reason) ? "No respondiste si estabas bien"
                : SafeTrip.REASON_UNCONFIRMED.equals(reason) ? "No confirmaste que llegaste bien"
                : "No confirmaste que llegaste a " + t.getDestination();
        try {
            push.sendData(tokens, Map.of("tipo", "ALERTA_PROPIA", "alertaId", String.valueOf(alertId), "motivo", why));
        } catch (RuntimeException e) {
            // Her contacts were already warned: this notice is only a convenience
            log.warn("No se pudo avisar a la usuaria de la alerta {}: {}", alertId, e.getMessage());
        }
    }

    /** What her contacts read when the automatic protection alerts. */
    private static String autoMessage(SafeTrip t, String reason) {
        String since = "Salió de uno de sus lugares frecuentes a las " + TIME.format(ColombiaTime.of(t.getStartedAt())) + ". ";
        String lastSignal = "Última señal a las " + TIME.format(ColombiaTime.of(t.getLastSeenAt())) + ".";
        String battery = t.getLowBatteryAt() != null ? " Su batería estaba por debajo del 10 %." : "";
        if (SafeTrip.REASON_UNCONFIRMED.equals(reason)) {
            return "Lleva un rato quieta en un mismo lugar y no confirmó que llegó bien. " + since + lastSignal;
        }
        return SafeTrip.REASON_NO_ANSWER.equals(reason)
                ? "Se detuvo en una zona de riesgo y no respondió si estaba bien. " + since + lastSignal
                : "Su celular dejó de enviar señal mientras iba de camino. " + since + lastSignal + battery;
    }

    // ── Automatic protection ─────────────────────────────────────

    @Override
    @Transactional
    public String enable(Long userId, Long deviceId) {
        Long profile = identity.requiredProfile(userId);
        protectedPhones.deleteOf(profile, deviceId);
        ProtectionDevice.Enrolled enrolled = ProtectionDevice.enroll(profile, userId, deviceId);
        protectedPhones.save(enrolled.device());
        return enrolled.key();
    }

    @Override
    @Transactional
    public void disable(Long userId, Long deviceId) {
        identity.profileOf(userId).ifPresent(profile -> protectedPhones.deleteOf(profile, deviceId));
    }

    @Override
    public boolean enabled(Long userId, Long deviceId) {
        return identity.profileOf(userId).map(profile -> protectedPhones.exists(profile, deviceId)).orElse(false);
    }

    @Override
    @Transactional
    public SafeTrip startAuto(String key, java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        ProtectionDevice phone = phoneOf(key);
        Optional<SafeTrip> current = trips.activeOf(phone.getUserProfileId());
        if (current.isPresent()) return current.get();
        return trips.save(SafeTrip.automatic(phone.getUserProfileId(), phone.getUserId(), phone.getDeviceId(),
                latitude, longitude, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public SafeTrip autoHeartbeat(String key, Long tripId, AutoHeartbeat point) {
        SafeTrip t = autoTrip(key, tripId);
        LocalDateTime now = LocalDateTime.now();
        if (point.latitude() != null && point.longitude() != null) {
            LocationLog l = LocationLog.created(t.getUserProfileId(), point.latitude(), point.longitude(), point.accuracy());
            l.recordTime(point.recordedAt(), now);
            if (!t.active()) l.setAlertId(t.getAlertId());
            locations.save(l);
        }
        if (!t.active()) return t;
        t.heartbeat(point.latitude(), point.longitude(), point.lowBattery(), now);
        return trips.save(t);
    }

    @Override
    @Transactional
    public SafeTrip endAuto(String key, Long tripId) {
        SafeTrip t = autoTrip(key, tripId);
        if (t.active()) {
            t.end(LocalDateTime.now());
            trips.save(t);
        }
        return t;
    }

    @Override
    @Transactional
    public SafeTrip unconfirmed(String key, Long tripId) {
        SafeTrip t = autoTrip(key, tripId);
        if (t.active()) raiseAlert(t, LocalDateTime.now(), SafeTrip.REASON_UNCONFIRMED);
        return t;
    }

    @Override
    @Transactional
    public SafeTrip noAnswer(String key, Long tripId) {
        SafeTrip t = autoTrip(key, tripId);
        if (t.active()) raiseAlert(t, LocalDateTime.now(), SafeTrip.REASON_NO_ANSWER);
        return t;
    }

    private ProtectionDevice phoneOf(String key) {
        if (key == null || key.isBlank()) throw new AccessDeniedException("Falta la llave de la protección automática");
        return protectedPhones.byKeyHash(ProtectionDevice.hash(key))
                .orElseThrow(() -> new AccessDeniedException("La protección automática está desactivada en este celular"));
    }

    private SafeTrip autoTrip(String key, Long tripId) {
        ProtectionDevice phone = phoneOf(key);
        SafeTrip t = trips.find(tripId).orElseThrow(() -> new ResourceNotFoundException("Protección no encontrada"));
        if (!t.automatic() || !t.esDe(phone.getUserProfileId())) throw new AccessDeniedException("No es tu protección");
        return t;
    }

    private void askIfArrived(SafeTrip t, LocalDateTime now) {
        List<String> tokens = phones.phoneTokens(t.getUserId());
        if (!tokens.isEmpty()) {
            push.sendData(tokens, Map.of(
                    "tipo", "TRAYECTO_POR_VENCER",
                    "trayectoId", String.valueOf(t.getId()),
                    "destino", t.getDestination(),
                    "minutos", String.valueOf(Math.max(0, Duration.between(now, t.getExpectedArrival()).toMinutes()))));
        }
        t.markReminder();
        trips.save(t);
    }

    private static long minutesSince(LocalDateTime moment, LocalDateTime now) {
        return Math.max(0, Duration.between(moment, now).toMinutes());
    }

    private SafeTrip own(Long id, Long userId) {
        Long profile = identity.requiredProfile(userId);
        return trips.find(id).filter(t -> t.esDe(profile))
                .orElseThrow(() -> new ResourceNotFoundException("Trayecto", id));
    }
}
