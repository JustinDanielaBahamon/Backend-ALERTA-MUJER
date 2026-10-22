package com.alertamujer.admin.application.port.out;

import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;

import java.util.List;

/** Output port to identity: account administration (identity stores the data and its rules). */
public interface AccountsPort {

    List<UserAccountView> listAll();

    /** identity validates the status, prevents an admin from blocking herself and repeated emails. */
    UserAccountView update(Long userId, UserAccountChanges changes, Long actor);

    /** Erases the personal data, closes the sessions and leaves the account inactive. */
    void anonymize(Long userId);
}
