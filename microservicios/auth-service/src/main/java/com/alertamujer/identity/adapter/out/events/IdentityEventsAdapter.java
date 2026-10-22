package com.alertamujer.identity.adapter.out.events;

import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.identity.application.port.out.IdentityEventsPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: eventos de Spring. AdminSessionEvent lo audita el módulo admin; el de
 * recuperación se reenvía por HTTP a Alert Service, que envía el correo.
 */
@Component
class IdentityEventsAdapter implements IdentityEventsPort {

    private final ApplicationEventPublisher publisher;

    IdentityEventsAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void adminSession(Long accountId, String action, String detail) {
        publisher.publishEvent(new AdminSessionEvent(accountId, action, detail));
    }

    @Override
    public void recoveryCode(String email, String name, String code, int validityMinutes) {
        publisher.publishEvent(new PasswordRecoveryRequestedEvent(email, name, code, validityMinutes));
    }

    @Override
    public void accountDeleted(Long userId, Long userProfileId) {
        publisher.publishEvent(new AccountDeletedEvent(userId, userProfileId));
    }
}
