package com.alertamujer.shared.web;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Las fechas se guardan en la zona del servidor (UTC). Los archivos que genera el servidor
 * (PDF, CSV) no pasan por un navegador que las convierta, así que se muestran en hora de Colombia.
 */
public final class ColombiaTime {

    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private ColombiaTime() {}

    public static LocalDateTime of(LocalDateTime serverTime) {
        return serverTime == null ? null
                : serverTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZONE).toLocalDateTime();
    }
}
