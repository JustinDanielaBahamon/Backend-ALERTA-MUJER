package com.alertamujer.admin.adapter.in.events;

import com.alertamujer.admin.application.port.in.RecordSecurityEventUseCase;
import com.alertamujer.identity.api.AdminSessionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Input adapter: listens to the session events identity publishes. It is synchronous so the
 * failed attempt is recorded even if the login ends in an error.
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
