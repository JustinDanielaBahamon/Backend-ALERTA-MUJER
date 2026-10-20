package com.alertamujer.alerts.application.port.in;

import java.time.Instant;

/**
 * "Actualizar ubicación del celular" desde la web: un push silencioso a los celulares de la propia
 * usuaria para que la app envíe su ubicación actual (la web luego consulta la última).
 */
public interface PedirUbicacionUseCase {

    /** @throws IllegalStateException si no tiene un celular con notificaciones o pidió hace muy poco */
    Resultado pedir(Long userId);

    record Resultado(int celulares, Instant solicitadoEn) {}
}
