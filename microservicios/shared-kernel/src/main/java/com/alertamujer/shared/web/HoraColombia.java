package com.alertamujer.shared.web;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Las fechas se guardan en la zona del servidor (UTC). Los archivos que genera el servidor
 * (PDF, CSV) no pasan por un navegador que las convierta, así que se muestran en hora de Colombia.
 */
public final class HoraColombia {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private HoraColombia() {}

    public static LocalDateTime de(LocalDateTime horaServidor) {
        return horaServidor == null ? null
                : horaServidor.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZONA).toLocalDateTime();
    }
}
