package com.alertamujer.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Push data keys and type values used by app versions released before the English refactor.
 * Phones that have not updated yet only understand those, so every push also carries the old keys:
 * without them an emergency alarm would not ring on an outdated phone.
 * Remove once the minimum app version (admin settings) is one that reads the English keys.
 */
public final class LegacyPushKeys {

    private static final Map<String, String> KEYS = Map.of(
            "type", "tipo",
            "alertId", "alertaId",
            "name", "nombre",
            "code", "codigo",
            "status", "estado",
            "tripId", "trayectoId",
            "destination", "destino",
            "minutes", "minutos",
            "phone", "celular");

    private static final Map<String, String> TYPES = Map.of(
            "SOS_ALERT", "ALERTA_SOS",
            "ALERT_ENDED", "ALERTA_FINALIZADA",
            "NEW_PHONE", "NUEVO_CELULAR",
            "TRIP_DUE_SOON", "TRAYECTO_POR_VENCER",
            "LOCATION_REQUEST", "PEDIR_UBICACION");

    private LegacyPushKeys() {
    }

    /** The same data plus a copy of each known key under its old name (and the old type value). */
    public static Map<String, String> withLegacyKeys(Map<String, String> data) {
        Map<String, String> all = new LinkedHashMap<>(data);
        data.forEach((key, value) -> {
            String old = KEYS.get(key);
            if (old == null || data.containsKey(old)) return;
            all.put(old, "type".equals(key) ? TYPES.getOrDefault(value, value) : value);
        });
        return all;
    }
}
