package com.alertamujer.evidence.adapter.in.events;

import com.alertamujer.evidence.application.port.in.CleanUpEvidenceUseCase;
import com.alertamujer.identity.api.AccountDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Input adapter: Auth Service reports over HTTP that an account was deleted. */
@Component
class EvidenceAccountDeletedListener {

    private final CleanUpEvidenceUseCase cleanup;

    EvidenceAccountDeletedListener(CleanUpEvidenceUseCase cleanup) {
        this.cleanup = cleanup;
    }

    @EventListener
    public void onAccountDeleted(AccountDeletedEvent e) {
        cleanup.removeForUser(e.userProfileId());
    }
}
