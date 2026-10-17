package com.alertamujer.alerts.application.service;

import com.alertamujer.config.FcmService;
import com.alertamujer.devices.api.DevicesApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Actualizar ubicación del celular" desde la web: manda un push silencioso a los celulares de la
 * propia usuaria para que la app envíe su ubicación actual (la web luego consulta /me/latest).
 * Sin esto la web solo veía el último punto enviado mientras la app estuvo abierta.
 */
@Service
public class PhoneLocationRequestService {

    static final long SEGUNDOS_ENTRE_PEDIDOS = 20;

    public record Resultado(int celulares, Instant solicitadoEn) {}

    private final DevicesApi devicesApi;
    private final FcmService fcmService;
    private final Clock clock;
    private final Map<Long, Instant> ultimoPedido = new ConcurrentHashMap<>();

    @Autowired
    public PhoneLocationRequestService(DevicesApi devicesApi, FcmService fcmService) {
        this(devicesApi, fcmService, Clock.systemUTC());
    }

    PhoneLocationRequestService(DevicesApi devicesApi, FcmService fcmService, Clock clock) {
        this.devicesApi = devicesApi;
        this.fcmService = fcmService;
        this.clock = clock;
    }

    /** @throws IllegalStateException si no tiene un celular con notificaciones o pidió hace muy poco */
    public Resultado pedir(Long userId) {
        Instant ahora = clock.instant();
        Instant anterior = ultimoPedido.get(userId);
        if (anterior != null && anterior.plusSeconds(SEGUNDOS_ENTRE_PEDIDOS).isAfter(ahora)) {
            throw new IllegalStateException("Ya se pidió la ubicación hace un momento. Espera unos segundos.");
        }
        List<String> tokens = devicesApi.pushTokensOfUser(userId);
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Tu celular no tiene la app con notificaciones activas. Abre AlertaMujer en el celular e inicia sesión.");
        }
        List<String> fallidos = fcmService.sendEmergencyAlert(tokens, Map.of("tipo", "PEDIR_UBICACION"));
        if (fallidos.size() == tokens.size()) {
            throw new IllegalStateException("No se pudo contactar al celular. Intenta de nuevo en un momento.");
        }
        ultimoPedido.put(userId, ahora);
        return new Resultado(tokens.size() - fallidos.size(), ahora);
    }
}
