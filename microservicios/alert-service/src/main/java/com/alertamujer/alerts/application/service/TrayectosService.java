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
    private final IdentityPort identidad;
    private final GestionarAlertasUseCase alerts;
    private final UbicacionRepositorio ubicaciones;
    private final CelularesPort celulares;
    private final PushPort push;

    public TrayectosService(TrayectoRepositorio trayectos, IdentityPort identidad, GestionarAlertasUseCase alerts,
                            UbicacionRepositorio ubicaciones, CelularesPort celulares, PushPort push) {
        this.trayectos = trayectos;
        this.identidad = identidad;
        this.alerts = alerts;
        this.ubicaciones = ubicaciones;
        this.celulares = celulares;
        this.push = push;
    }

    @Override
    @Transactional
    public SafeTrip iniciar(NuevoTrayecto nuevo, Long userId) {
        Long perfil = identidad.perfilRequerido(userId);
        if (trayectos.activoDe(perfil).isPresent()) {
            throw new ConflictException("Ya tienes un trayecto en curso: confirma tu llegada o cancélalo primero");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (trayectos.iniciadosDesde(perfil, ahora.minusHours(24)) >= MAXIMO_POR_DIA) {
            throw new IllegalArgumentException("Llegaste al máximo de " + MAXIMO_POR_DIA + " trayectos por día");
        }
        return trayectos.save(SafeTrip.nuevo(perfil, userId, nuevo.deviceId(), nuevo.target(), nuevo.minutos(),
                nuevo.latitude(), nuevo.longitude(), ahora));
    }

    @Override
    public Optional<SafeTrip> activo(Long userId) {
        return identidad.profileOf(userId).flatMap(trayectos::activoDe);
    }

    @Override
    @Transactional
    public SafeTrip llegue(Long id, Long userId) {
        SafeTrip t = propio(id, userId);
        t.llegar(LocalDateTime.now());
        return trayectos.save(t);
    }

    @Override
    @Transactional
    public SafeTrip extender(Long id, int minutos, Long userId) {
        SafeTrip t = propio(id, userId);
        t.extender(minutos);
        return trayectos.save(t);
    }

    @Override
    @Transactional
    public SafeTrip cancelar(Long id, Long userId) {
        SafeTrip t = propio(id, userId);
        t.cancelar(LocalDateTime.now());
        return trayectos.save(t);
    }

    // ── Revisión periódica ──────────────────────────────────────────

    @Override
    @Transactional
    public int revisar(LocalDateTime ahora) {
        int alertadas = 0;
        for (SafeTrip t : trayectos.activosQueLleganAntesDe(ahora.plus(SafeTrip.AVISO_PREVIO))) {
            try {
                if (t.vencido(ahora)) {
                    alertar(t, ahora);
                    alertadas++;
                } else if (t.porVencer(ahora)) {
                    preguntarSiLlego(t, ahora);
                }
            } catch (RuntimeException e) {
                // Un trayecto con problemas no debe impedir revisar los demás
                log.error("No se pudo revisar el trayecto {}: {}", t.getId(), e.getMessage());
            }
        }
        return alertadas;
    }

    private void alertar(SafeTrip t, LocalDateTime ahora) {
        Optional<LocationLog> ultima = ubicaciones.ultima(t.getUserProfileId());
        String message = "No confirmó que llegó a " + t.getDestination() + ". Salió a las "
                + HORA.format(ColombiaTime.of(t.getStartedAt())) + " y debía llegar a las "
                + HORA.format(ColombiaTime.of(t.getExpectedArrival())) + "."
                + ultima.map(l -> " Última ubicación hace " + minutosDesde(l.getRecordedAt(), ahora) + " min.").orElse("");
        Alert alert = alerts.activar(new NuevaAlerta(t.getDeviceId(), "main", "timer", "active", message,
                ultima.map(LocationLog::getLatitude).orElse(t.getDestinationLatitude()),
                ultima.map(LocationLog::getLongitude).orElse(t.getDestinationLongitude()),
                null, null), t.getUserId());
        t.marcarAlertado(alert.getId(), ahora);
        trayectos.save(t);
        log.info("Trayecto {} sin confirmar: alerta {} activada", t.getId(), alert.getId());
    }

    private void preguntarSiLlego(SafeTrip t, LocalDateTime ahora) {
        List<String> tokens = celulares.tokensDeCelulares(t.getUserId());
        if (!tokens.isEmpty()) {
            push.enviarDatos(tokens, Map.of(
                    "tipo", "TRAYECTO_POR_VENCER",
                    "trayectoId", String.valueOf(t.getId()),
                    "destino", t.getDestination(),
                    "minutos", String.valueOf(Math.max(0, Duration.between(ahora, t.getExpectedArrival()).toMinutes()))));
        }
        t.marcarRecordatorio();
        trayectos.save(t);
    }

    private static long minutosDesde(LocalDateTime momento, LocalDateTime ahora) {
        return Math.max(0, Duration.between(momento, ahora).toMinutes());
    }

    private SafeTrip propio(Long id, Long userId) {
        Long perfil = identidad.perfilRequerido(userId);
        return trayectos.find(id).filter(t -> t.esDe(perfil))
                .orElseThrow(() -> new ResourceNotFoundException("Trayecto", id));
    }
}
