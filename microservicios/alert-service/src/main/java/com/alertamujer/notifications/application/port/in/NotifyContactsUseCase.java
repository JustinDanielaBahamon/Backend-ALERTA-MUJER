package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * Avisa a los contactos de emergencia: al activar la alerta, correo con el enlace de seguimiento y
 * push de alta prioridad a quienes tienen la app; al terminar, un aviso para que no sigan pendientes.
 */
public interface NotifyContactsUseCase {

    /** Deja el resultado de cada envío en la alerta (sent / failed). */
    void alertActivated(Long alertId, Long userProfile, LocalDateTime start);

    /** Aviso de cortesía: no cambia el estado de los envíos y un fallo solo se registra en el log. */
    void alertEnded(Long alertId, Long userProfile, String status, LocalDateTime end);
}
