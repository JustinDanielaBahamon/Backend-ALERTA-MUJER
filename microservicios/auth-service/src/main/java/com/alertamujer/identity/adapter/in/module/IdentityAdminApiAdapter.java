package com.alertamujer.identity.adapter.in.module;

import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase;
import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase.CuentaDeUsuaria;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosCuenta;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de entrada: el contrato IdentityAdminApi que usa el panel (módulo admin). */
@Component
class IdentityAdminApiAdapter implements IdentityAdminApi {

    private final AdministrarCuentasUseCase cuentas;

    IdentityAdminApiAdapter(AdministrarCuentasUseCase cuentas) {
        this.cuentas = cuentas;
    }

    @Override
    public List<UserAccountView> listUserAccounts() {
        return cuentas.listAll().stream().map(IdentityAdminApiAdapter::view).toList();
    }

    @Override
    public UserAccountView updateUserAccount(Long userId, UserAccountChanges c, Long actingUserId) {
        return view(cuentas.actualizar(userId,
                new CambiosCuenta(c.accountStatus(), c.firstName(), c.lastName(), c.telephone(), c.email()), actingUserId));
    }

    @Override
    public void anonymizeAccount(Long userId) {
        cuentas.anonimizar(userId);
    }

    static UserAccountView view(CuentaDeUsuaria cu) {
        User u = cu.usuaria();
        Account a = cu.cuenta();
        return new UserAccountView(u.getId(), cu.perfilId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getTelephone(), u.getRoleName(), a != null ? a.getStatus() : null, u.getCreatedAt(),
                a != null ? a.getLastAccess() : null);
    }
}
