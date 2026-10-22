package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.RequestLocationUseCase;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.PushPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pide a los celulares de la usuaria su ubicación actual con un push silencioso. Sin esto la web
 * solo veía el último punto enviado mientras la app estuvo abierta.
 */
@Service
public class LocationRequestService implements RequestLocationUseCase {

    static final long SECONDS_BETWEEN_REQUESTS = 20;

    private final PhonesPort phones;
    private final PushPort push;
    private final Clock clock;
    private final Map<Long, Instant> lastRequest = new ConcurrentHashMap<>();

    @Autowired
    public LocationRequestService(PhonesPort phones, PushPort push) {
        this(phones, push, Clock.systemUTC());
    }

    LocationRequestService(PhonesPort phones, PushPort push, Clock clock) {
        this.phones = phones;
        this.push = push;
        this.clock = clock;
    }

    @Override
    public Result perform(Long userId) {
        Instant now = clock.instant();
        Instant previous = lastRequest.get(userId);
        if (previous != null && previous.plusSeconds(SECONDS_BETWEEN_REQUESTS).isAfter(now)) {
            throw new IllegalStateException("Ya se pidió la ubicación hace un momento. Espera unos segundos.");
        }
        // Solo celulares: un navegador no tiene GPS ni puede responder
        List<String> tokens = phones.phoneTokens(userId);
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Tu celular no tiene la app con notificaciones activas. Abre AlertaMujer en el celular e inicia sesión.");
        }
        List<String> failedOnes = push.sendData(tokens, Map.of("tipo", "PEDIR_UBICACION"));
        if (failedOnes.size() == tokens.size()) {
            throw new IllegalStateException("No se pudo contactar al celular. Intenta de nuevo en un momento.");
        }
        lastRequest.put(userId, now);
        return new Result(tokens.size() - failedOnes.size(), now);
    }
}
