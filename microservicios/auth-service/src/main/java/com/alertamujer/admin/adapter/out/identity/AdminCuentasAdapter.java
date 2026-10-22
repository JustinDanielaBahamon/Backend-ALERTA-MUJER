package com.alertamujer.admin.adapter.out.identity;

import com.alertamujer.admin.application.port.out.CuentasPort;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida hacia identity (mismo microservicio) por su contrato IdentityAdminApi. */
@Component
class AdminCuentasAdapter implements CuentasPort {

    private final IdentityAdminApi identityAdminApi;

    AdminCuentasAdapter(IdentityAdminApi identityAdminApi) {
        this.identityAdminApi = identityAdminApi;
    }

    @Override
    public List<UserAccountView> listAll() {
        return identityAdminApi.listUserAccounts();
    }

    @Override
    public UserAccountView actualizar(Long userId, UserAccountChanges cambios, Long quienActua) {
        return identityAdminApi.updateUserAccount(userId, cambios, quienActua);
    }

    @Override
    public void anonimizar(Long userId) {
        identityAdminApi.anonymizeAccount(userId);
    }
}
