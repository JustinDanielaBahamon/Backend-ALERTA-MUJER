package com.alertamujer.alerts.application.port.out;

import java.util.List;

/** Puerto de salida: los celulares de la usuaria (Auth Service). */
public interface CelularesPort {

    /** true si el dispositivo es el celular que la usuaria usó más recientemente (o si no se conoce). */
    boolean esElMasReciente(Long userId, Long deviceId);

    /** Tokens push solo de celulares (sin navegadores web). */
    List<String> phoneTokens(Long userId);
}
