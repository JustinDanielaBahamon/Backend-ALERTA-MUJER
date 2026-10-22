package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.AlertMessage;

import java.util.List;

/**
 * Chat de una alerta: quien la activó y sus contactos se escriben mientras está activa (mensajes
 * rápidos predeterminados o texto libre). Los contactos llegan por su enlace de seguimiento.
 */
public interface ChatAlertaUseCase {

    /** Mensajes posteriores a afterId (null o 0 = todos). */
    List<AlertMessage> mensajes(Long alertId, Long afterId);

    /** Igual, pero solo para la dueña (una alerta ajena se responde como inexistente). */
    List<AlertMessage> mensajesDeLaDuena(Long alertId, Long userId, Long afterId);

    AlertMessage enviarDeLaDuena(Long alertId, Long userId, String texto);

    AlertMessage enviarDeContacto(Long alertId, String name, String texto);
}
