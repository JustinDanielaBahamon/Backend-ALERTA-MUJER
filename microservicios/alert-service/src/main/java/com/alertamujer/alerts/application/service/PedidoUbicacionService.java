package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.PedirUbicacionUseCase;
import com.alertamujer.alerts.application.port.out.CelularesPort;
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
public class PedidoUbicacionService implements PedirUbicacionUseCase {

    static final long SEGUNDOS_ENTRE_PEDIDOS = 20;

    private final CelularesPort celulares;
    private final PushPort push;
    private final Clock clock;
    private final Map<Long, Instant> ultimoPedido = new ConcurrentHashMap<>();

    @Autowired
    public PedidoUbicacionService(CelularesPort celulares, PushPort push) {
        this(celulares, push, Clock.systemUTC());
    }

    PedidoUbicacionService(CelularesPort celulares, PushPort push, Clock clock) {
        this.celulares = celulares;
        this.push = push;
        this.clock = clock;
    }

    @Override
    public Resultado perform(Long userId) {
        Instant ahora = clock.instant();
        Instant anterior = ultimoPedido.get(userId);
        if (anterior != null && anterior.plusSeconds(SEGUNDOS_ENTRE_PEDIDOS).isAfter(ahora)) {
            throw new IllegalStateException("Ya se pidió la ubicación hace un momento. Espera unos segundos.");
        }
        // Solo celulares: un navegador no tiene GPS ni puede responder
        List<String> tokens = celulares.tokensDeCelulares(userId);
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Tu celular no tiene la app con notificaciones activas. Abre AlertaMujer en el celular e inicia sesión.");
        }
        List<String> fallidos = push.enviarDatos(tokens, Map.of("tipo", "PEDIR_UBICACION"));
        if (fallidos.size() == tokens.size()) {
            throw new IllegalStateException("No se pudo contactar al celular. Intenta de nuevo en un momento.");
        }
        ultimoPedido.put(userId, ahora);
        return new Resultado(tokens.size() - fallidos.size(), ahora);
    }
}
