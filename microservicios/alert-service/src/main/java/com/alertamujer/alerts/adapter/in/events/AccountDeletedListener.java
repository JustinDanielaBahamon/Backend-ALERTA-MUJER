package com.alertamujer.alerts.adapter.in.events;

import com.alertamujer.alerts.application.port.in.DeleteUserDataUseCase;
import com.alertamujer.identity.api.AccountDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: Auth Service avisa por HTTP que se eliminó una cuenta. */
@Component
class AccountDeletedListener {

    private final DeleteUserDataUseCase deletion;

    AccountDeletedListener(DeleteUserDataUseCase deletion) {
        this.deletion = deletion;
    }

    @EventListener
    public void onAccountDeleted(AccountDeletedEvent e) {
        deletion.deleteDataOf(e.userProfileId());
    }
}
