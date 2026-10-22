package com.alertamujer.identity.api;

import java.util.List;

/** Account administration operations the identity module offers to the panel. */
public interface IdentityAdminApi {

    List<UserAccountView> listUserAccounts();

    /**
     * Applies the changes to the person and her account. Validates the account status, prevents an
     * admin from blocking herself (actingUserId) and keeps the email unique.
     */
    UserAccountView updateUserAccount(Long userId, UserAccountChanges changes, Long actingUserId);

    /**
     * "Deletes" the account without breaking the history: erases the personal data (name, email,
     * phone, document, photo), closes her sessions and leaves it inactive so nobody can sign in.
     */
    void anonymizeAccount(Long userId);
}
