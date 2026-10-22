package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.AccountChanges;
import com.alertamujer.identity.domain.model.User;

import java.util.List;

/** Account administration from the panel. */
public interface ManageAccountsUseCase {

    List<UserAccount> listAll();

    /** Validates the status, prevents an admin from blocking herself and keeps the email unique. */
    UserAccount update(Long userId, AccountChanges changes, Long actor);

    /** Erases the personal data, closes her sessions and leaves her inactive so she cannot sign in. */
    void anonymize(Long userId);

    record UserAccount(User user, Account account, Long profileId) {}
}
