package com.alertamujer.admin.adapter.out.identity;

import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia identity (mismo microservicio) por su contrato IdentityApi. */
@Component
class AdminUsersAdapter implements UsersPort {

    private final IdentityApi identityApi;

    AdminUsersAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<UserSummary> find(Long userId) {
        return identityApi.findUser(userId);
    }

    @Override
    public Optional<UserSummary> withEmail(String email) {
        return identityApi.findUserByEmail(email);
    }

    @Override
    public Map<Long, UserSummary> byId() {
        return identityApi.usersById();
    }

    @Override
    public Map<Long, UserSummary> byAccount() {
        return identityApi.usersByAccountId();
    }
}
