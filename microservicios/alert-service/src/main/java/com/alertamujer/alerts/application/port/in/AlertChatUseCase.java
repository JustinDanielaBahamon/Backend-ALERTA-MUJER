package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.AlertMessage;

import java.util.List;

/**
 * Chat de una alerta: quien la activó y sus contactos se escriben mientras está activa (mensajes
 * rápidos predeterminados o texto libre). Los contactos llegan por su enlace de seguimiento.
 */
public interface AlertChatUseCase {

    /** Mensajes posteriores a afterId (null o 0 = todos). */
    List<AlertMessage> messages(Long alertId, Long afterId);

    /** Igual, pero solo para la dueña (una alerta ajena se responde como inexistente). */
    List<AlertMessage> ownerMessages(Long alertId, Long userId, Long afterId);

    AlertMessage sendFromOwner(Long alertId, Long userId, String text);

    AlertMessage sendFromContact(Long alertId, String name, String text);
}
