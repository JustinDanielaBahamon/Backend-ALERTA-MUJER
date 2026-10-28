package com.alertamujer.admin.adapter.out.identity;

import com.alertamujer.admin.application.port.out.AccountsPort;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.springframework.stereotype.Component;

import java.util.List;

/** Output adapter to identity (same microservice) through its IdentityAdminApi contract. */
@Component
class AdminAccountsAdapter implements AccountsPort {

    private final IdentityAdminApi identityAdminApi;

    AdminAccountsAdapter(IdentityAdminApi identityAdminApi) {
        this.identityAdminApi = identityAdminApi;
    }

    @Override
    public List<UserAccountView> listAll() {
        return identityAdminApi.listUserAccounts();
    }

    @Override
    public UserAccountView update(Long userId, UserAccountChanges changes, Long actor) {
        return identityAdminApi.updateUserAccount(userId, changes, actor);
    }

    @Override
    public void anonymize(Long userId) {
        identityAdminApi.anonymizeAccount(userId);
    }
}
