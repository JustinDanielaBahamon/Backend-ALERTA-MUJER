package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.GestionarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.in.RevisarTrayectosUseCase;
import com.alertamujer.alerts.application.port.in.TrayectosUseCase;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.PushPort;
import com.alertamujer.alerts.application.port.out.TrayectoRepositorio;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
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
public class TrayectosService implements TrayectosUseCase, RevisarTrayectosUseCase {

    private static final Logger log = LoggerFactory.getLogger(TrayectosService.class);
    /** Evita abusos (miles de temporizadores) sin estorbar el uso normal. */
    static final int MAXIMO_POR_DIA = 20;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a");

    private final TrayectoRepositorio trayectos;
    private final IdentityPort identity;
    private final GestionarAlertasUseCase alerts;
    private final UbicacionRepositorio ubicaciones;
    private final CelularesPort phones;
    private final PushPort push;

    public TrayectosService(TrayectoRepositorio trayectos, IdentityPort identity, GestionarAlertasUseCase alerts,
                            UbicacionRepositorio ubicaciones, CelularesPort phones, PushPort push) {
        this.trayectos = trayectos;
        this.identity = identity;
        this.alerts = alerts;
        this.ubicaciones = ubicaciones;
        this.phones = phones;
        this.push = push;
    }

    @Override
    @Transactional
    public SafeTrip iniciar(NuevoTrayecto created, Long userId) {
        Long profile = identity.perfilRequerido(userId);
        if (trayectos.activoDe(profile).isPresent()) {
            throw new ConflictException("Ya tienes un trayecto en curso: confirma tu llegada o cancélalo primero");
        }
        LocalDateTime now = LocalDateTime.now();
        if (trayectos.iniciadosDesde(profile, now.minusHours(24)) >= MAXIMO_POR_DIA) {
            throw new IllegalArgumentException("Llegaste al máximo de " + MAXIMO_POR_DIA + " trayectos por día");
        }
        return trayectos.save(SafeTrip.created(profile, userId, created.deviceId(), created.target(), created.minutes(),
                created.latitude(), created.longitude(), now));
    }

    @Override
    public Optional<SafeTrip> activo(Long userId) {
        return identity.profileOf(userId).flatMap(trayectos::activoDe);
    }

    @Override
    @Transactional
    public SafeTrip llegue(Long id, Long userId) {
        SafeTrip t = own(id, userId);
        t.llegar(LocalDateTime.now());
        return trayectos.save(t);
    }

    @Override
    @Transactional
    public SafeTrip extender(Long id, int minutes, Long userId) {
        SafeTrip t = own(id, userId);
        t.extender(minutes);
        return trayectos.save(t);
    }

    @Override
    @Transactional
    public SafeTrip cancelar(Long id, Long userId) {
        SafeTrip t = own(id, userId);
        t.cancelar(LocalDateTime.now());
        return trayectos.save(t);
    }

    // ── Revisión periódica ──────────────────────────────────────────

    @Override
    @Transactional
    public int revisar(LocalDateTime now) {
        int alertadas = 0;
        for (SafeTrip t : trayectos.activosQueLleganAntesDe(now.plus(SafeTrip.AVISO_PREVIO))) {
            try {
                if (t.vencido(now)) {
                    alertar(t, now);
                    alertadas++;
                } else if (t.porVencer(now)) {
                    preguntarSiLlego(t, now);
                }
            } catch (RuntimeException e) {
                // Un trayecto con problemas no debe impedir revisar los demás
                log.error("No se pudo revisar el trayecto {}: {}", t.getId(), e.getMessage());
            }
        }
        return alertadas;
    }

    private void alertar(SafeTrip t, LocalDateTime now) {
        Optional<LocationLog> ultima = ubicaciones.ultima(t.getUserProfileId());
        String message = "No confirmó que llegó a " + t.getDestination() + ". Salió a las "
                + HORA.format(ColombiaTime.of(t.getStartedAt())) + " y debía llegar a las "
                + HORA.format(ColombiaTime.of(t.getExpectedArrival())) + "."
                + ultima.map(l -> " Última ubicación hace " + minutosDesde(l.getRecordedAt(), now) + " min.").orElse("");
        Alert alert = alerts.activar(new NuevaAlerta(t.getDeviceId(), "main", "timer", "active", message,
                ultima.map(LocationLog::getLatitude).orElse(t.getDestinationLatitude()),
                ultima.map(LocationLog::getLongitude).orElse(t.getDestinationLongitude()),
                null, null), t.getUserId());
        t.marcarAlertado(alert.getId(), now);
        trayectos.save(t);
        log.info("Trayecto {} sin confirmar: alerta {} activada", t.getId(), alert.getId());
    }

    private void preguntarSiLlego(SafeTrip t, LocalDateTime now) {
        List<String> tokens = phones.phoneTokens(t.getUserId());
        if (!tokens.isEmpty()) {
            push.enviarDatos(tokens, Map.of(
                    "tipo", "TRAYECTO_POR_VENCER",
                    "trayectoId", String.valueOf(t.getId()),
                    "destino", t.getDestination(),
                    "minutos", String.valueOf(Math.max(0, Duration.between(now, t.getExpectedArrival()).toMinutes()))));
        }
        t.marcarRecordatorio();
        trayectos.save(t);
    }

    private static long minutosDesde(LocalDateTime momento, LocalDateTime now) {
        return Math.max(0, Duration.between(momento, now).toMinutes());
    }

    private SafeTrip own(Long id, Long userId) {
        Long profile = identity.perfilRequerido(userId);
        return trayectos.find(id).filter(t -> t.esDe(profile))
                .orElseThrow(() -> new ResourceNotFoundException("Trayecto", id));
    }
}
