package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AccountDeletionRequest;

import java.util.List;
import java.util.Optional;

/** Output port: account deletion requests. */
public interface DeletionRequestRepository {

    /** Newest to oldest. */
    List<AccountDeletionRequest> all();

    Optional<AccountDeletionRequest> find(Long id);

    boolean hasPending(Long userId);

    AccountDeletionRequest save(AccountDeletionRequest deletionRequest);
}
