package com.alertamujer.alerts.adapter.in.events;

import com.alertamujer.alerts.application.port.in.DeleteUserDataUseCase;
import com.alertamujer.identity.api.AccountDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Input adapter: Auth Service reports over HTTP that an account was deleted. */
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
