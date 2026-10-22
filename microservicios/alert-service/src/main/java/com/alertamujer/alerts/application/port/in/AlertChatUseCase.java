package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.AlertMessage;

import java.util.List;

/**
 * Alert chat: whoever activated it and her contacts write to each other while it is active (preset
 * quick messages or free text). The contacts arrive through their tracking link.
 */
public interface AlertChatUseCase {

    /** Mensajes posteriores a afterId (null o 0 = todos). */
    List<AlertMessage> messages(Long alertId, Long afterId);

    /** Same, but only for the owner (someone else's alert is answered as missing). */
    List<AlertMessage> ownerMessages(Long alertId, Long userId, Long afterId);

    AlertMessage sendFromOwner(Long alertId, Long userId, String text);

    AlertMessage sendFromContact(Long alertId, String name, String text);
}
