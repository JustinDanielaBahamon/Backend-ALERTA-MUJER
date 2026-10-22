package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.in.ReviewTripsUseCase;
import com.alertamujer.alerts.application.port.in.TripsUseCase;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.PushPort;
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
 * "Avísame si no llego". Si la usuaria no confirma su llegada a tiempo, se activa una alerta normal
 * (correo y push a sus contactos) con su última ubicación conocida: funciona aunque su celular esté
 * sin datos ni saldo, porque la activa el servidor.
 */
@Service
public class TripsService implements TripsUseCase, ReviewTripsUseCase {

    private static final Logger log = LoggerFactory.getLogger(TripsService.class);
    /** Evita abusos (miles de temporizadores) sin estorbar el uso normal. */
    static final int MAX_PER_DAY = 20;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");

    private final TripRepository trips;
    private final IdentityPort identity;
    private final ManageOwnAlertsUseCase alerts;
    private final LocationRepository locations;
    private final PhonesPort phones;
    private final PushPort push;

    public TripsService(TripRepository trips, IdentityPort identity, ManageOwnAlertsUseCase alerts,
                            LocationRepository locations, PhonesPort phones, PushPort push) {
        this.trips = trips;
        this.identity = identity;
        this.alerts = alerts;
        this.locations = locations;
        this.phones = phones;
        this.push = push;
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

    // ── Revisión periódica ──────────────────────────────────────────

    @Override
    @Transactional
    public int review(LocalDateTime now) {
        int alerted = 0;
        for (SafeTrip t : trips.activeArrivingBefore(now.plus(SafeTrip.REMINDER_LEAD))) {
            try {
                if (t.expired(now)) {
                    raiseAlert(t, now);
                    alerted++;
                } else if (t.dueSoon(now)) {
                    askIfArrived(t, now);
                }
            } catch (RuntimeException e) {
                // Un trayecto con problemas no debe impedir revisar los demás
                log.error("No se pudo revisar el trayecto {}: {}", t.getId(), e.getMessage());
            }
        }
        return alerted;
    }

    private void raiseAlert(SafeTrip t, LocalDateTime now) {
        Optional<LocationLog> latest = locations.latest(t.getUserProfileId());
        String message = "No confirmó que llegó a " + t.getDestination() + ". Salió a las "
                + TIME.format(ColombiaTime.of(t.getStartedAt())) + " y debía llegar a las "
                + TIME.format(ColombiaTime.of(t.getExpectedArrival())) + "."
                + latest.map(l -> " Última ubicación hace " + minutesSince(l.getRecordedAt(), now) + " min.").orElse("");
        Alert alert = alerts.activate(new NewAlert(t.getDeviceId(), "main", "timer", "active", message,
                latest.map(LocationLog::getLatitude).orElse(t.getDestinationLatitude()),
                latest.map(LocationLog::getLongitude).orElse(t.getDestinationLongitude()),
                null, null), t.getUserId());
        t.markAlerted(alert.getId(), now);
        trips.save(t);
        log.info("Trayecto {} sin confirmar: alerta {} activada", t.getId(), alert.getId());
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
