package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.CambiosAlerta;

import java.util.List;
import java.util.Optional;

/** Casos de uso de la usuaria con sus alertas. */
public interface GestionarAlertasUseCase {

    /** De la más reciente a la más antigua. */
    List<Alert> mias(Long userId);

    /** Vacío si no existe o es de otra usuaria. */
    Optional<Alert> una(Long id, Long userId);

    /**
     * Alertas de una usuaria. Los clientes envían el id de usuaria (users.id); también se acepta el
     * id de perfil propio. Solo la administración ve las de otra usuaria.
     */
    List<Alert> deUsuaria(Long idPedido, Long userId);

    /**
     * Activa una alerta y registra a quién se avisa. Solo puede haber una en curso por usuaria: si
     * ya hay una activa se devuelve esa, sin crear otra ni volver a avisar a los contactos.
     */
    Alert activar(NuevaAlerta nueva, Long userId);

    Alert actualizar(Long id, CambiosAlerta cambios, Long userId);

    /** Conservar (o no) las evidencias de la alerta más allá de 60 días. */
    Alert conservarEvidencias(Long id, boolean conservar, Long userId);

    void delete(Long id, Long userId);
}
