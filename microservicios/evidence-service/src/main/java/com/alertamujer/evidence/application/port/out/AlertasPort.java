package com.alertamujer.evidence.application.port.out;

import com.alertamujer.alerts.api.AlertSummary;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida: lo que las evidencias necesitan saber de las alertas. Hoy lo resuelve el módulo
 * alerts en memoria (mismo microservicio alert-service).
 */
public interface AlertasPort {

    Optional<AlertSummary> buscar(Long alertId);

    Map<Long, AlertSummary> buscarVarias(Collection<Long> alertIds);

    Map<Long, AlertSummary> todas();

    /** Ids de las alertas de una usuaria (user_profile.id). */
    java.util.List<Long> idsDeLaUsuaria(Long userProfileId);

    /** De esas alertas, las que la usuaria marcó para conservar sus evidencias. */
    Set<Long> conservanEvidencias(Collection<Long> alertIds);
}
