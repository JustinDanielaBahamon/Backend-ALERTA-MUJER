package com.alertamujer.admin.adapter.in.events;

import com.alertamujer.admin.application.port.in.RecordSecurityEventUseCase;
import com.alertamujer.identity.api.AdminSessionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: escucha los eventos de sesión que publica identity. Es síncrono para que el
 * intento fallido quede registrado aunque el login termine en error.
 */
@Component
class AdminSessionListener {

    private final RecordSecurityEventUseCase events;

    AdminSessionListener(RecordSecurityEventUseCase events) {
        this.events = events;
    }

    @EventListener
    public void onSessionEvent(AdminSessionEvent event) {
        events.registrar(event.accountId(), event.action(), event.detail());
    }
}
