package com.alertamujer.identity.adapter.in.module;

import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import com.alertamujer.identity.application.port.in.ManageAccountsUseCase;
import com.alertamujer.identity.application.port.in.ManageAccountsUseCase.UserAccount;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.AccountChanges;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de entrada: el contrato IdentityAdminApi que usa el panel (módulo admin). */
@Component
class IdentityAdminApiAdapter implements IdentityAdminApi {

    private final ManageAccountsUseCase accounts;

    IdentityAdminApiAdapter(ManageAccountsUseCase accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<UserAccountView> listUserAccounts() {
        return accounts.listAll().stream().map(IdentityAdminApiAdapter::view).toList();
    }

    @Override
    public UserAccountView updateUserAccount(Long userId, UserAccountChanges c, Long actingUserId) {
        return view(accounts.update(userId,
                new AccountChanges(c.accountStatus(), c.firstName(), c.lastName(), c.telephone(), c.email()), actingUserId));
    }

    @Override
    public void anonymizeAccount(Long userId) {
        accounts.anonymize(userId);
    }

    static UserAccountView view(UserAccount cu) {
        User u = cu.user();
        Account a = cu.account();
        return new UserAccountView(u.getId(), cu.profileId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getTelephone(), u.getRoleName(), a != null ? a.getStatus() : null, u.getCreatedAt(),
                a != null ? a.getLastAccess() : null);
    }
}
