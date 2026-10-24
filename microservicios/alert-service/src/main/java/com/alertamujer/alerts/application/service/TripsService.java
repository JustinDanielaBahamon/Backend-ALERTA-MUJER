package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.in.ReviewTripsUseCase;
import com.alertamujer.alerts.application.port.in.TripsUseCase;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.PushPort;
import com.alertamujer.alerts.application.port.out.RiskZonesPort;
import com.alertamujer.alerts.application.port.out.TripRepository;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.domain.model.SafeTrip;
import com.alertamujer.shared.exception.ConflictException;
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
import java.util.Map;
import java.util.Optional;

/**
 * "Avísame si no llego". If the user does not confirm her arrival in time, a normal alert is activated
 * (email and push to her contacts) with her last known location: it works even if her phone has
 * no data or credit, because the server activates it. During the trip her phone sends signals of life:
 * if they stop (phone off, no signal, taken away) the alert goes out before the arrival time, sooner
 * inside a high-risk zone.
 */
@Service
public class TripsService implements TripsUseCase, ReviewTripsUseCase {

    private static final Logger log = LoggerFactory.getLogger(TripsService.class);
    /** Prevents abuse (thousands of timers) without getting in the way of normal use. */
    static final int MAX_PER_DAY = 20;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");

    private final TripRepository trips;
    private final IdentityPort identity;
    private final ManageOwnAlertsUseCase alerts;
    private final LocationRepository locations;
    private final PhonesPort phones;
    private final PushPort push;
    private final RiskZonesPort riskZones;

    public TripsService(TripRepository trips, IdentityPort identity, ManageOwnAlertsUseCase alerts,
                            LocationRepository locations, PhonesPort phones, PushPort push, RiskZonesPort riskZones) {
        this.trips = trips;
        this.identity = identity;
        this.alerts = alerts;
        this.locations = locations;
        this.phones = phones;
        this.push = push;
        this.riskZones = riskZones;
    }

    @Override
    @Transactional
    public SafeTrip start(NewTrip created, Long userId) {
        Long profile = identity.requiredProfile(userId);
        if (trips.activeOf(profile).isPresent()) {
            throw new ConflictException("Ya tienes un trayecto en curso: confirma tu llegada o cancélalo primero");
        }
        LocalDateTime now = LocalDateTime.now();
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
                if (t.expired(now)) {
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
        return riskZones.isHighRisk(t.getLastLatitude(), t.getLastLongitude())
                ? SafeTrip.SIGNAL_TIMEOUT_RISK_ZONE : SafeTrip.SIGNAL_TIMEOUT;
    }

    private void raiseAlert(SafeTrip t, LocalDateTime now, String reason) {
        Optional<LocationLog> latest = locations.latest(t.getUserProfileId());
        boolean signalLost = SafeTrip.REASON_SIGNAL_LOST.equals(reason);
        String message = signalLost
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
        Alert alert = alerts.activate(new NewAlert(t.getDeviceId(), "main", signalLost ? "signal_lost" : "timer",
                "active", message, lat, lng, null, null), t.getUserId());
        t.markAlerted(alert.getId(), now, reason);
        trips.save(t);
        log.info("Trayecto {} ({}): alerta {} activada", t.getId(), reason, alert.getId());
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
