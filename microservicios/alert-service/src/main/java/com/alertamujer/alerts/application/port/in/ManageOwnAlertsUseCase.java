package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertChanges;

import java.util.List;
import java.util.Optional;

/** Casos de uso de la usuaria con sus alertas. */
public interface ManageOwnAlertsUseCase {

    /** De la más reciente a la más antigua. */
    List<Alert> mias(Long userId);

    /** Vacío si no existe o es de otra usuaria. */
    Optional<Alert> one(Long id, Long userId);

    /**
     * Alertas de una usuaria. Los clientes envían el id de usuaria (users.id); también se acepta el
     * id de perfil propio. Solo la administración ve las de otra usuaria.
     */
    List<Alert> ofUser(Long requestedId, Long userId);

    /**
     * Activa una alerta y registra a quién se avisa. Solo puede haber una en curso por usuaria: si
     * ya hay una activa se devuelve esa, sin crear otra ni volver a avisar a los contactos.
     */
    Alert activate(NewAlert newOne, Long userId);

    Alert update(Long id, AlertChanges changes, Long userId);

    /** Conservar (o no) las evidencias de la alerta más allá de 60 días. */
    Alert keepEvidence(Long id, boolean keep, Long userId);

    void delete(Long id, Long userId);
}
