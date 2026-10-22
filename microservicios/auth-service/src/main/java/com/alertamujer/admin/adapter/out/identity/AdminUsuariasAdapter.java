package com.alertamujer.admin.adapter.out.identity;

import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia identity (mismo microservicio) por su contrato IdentityApi. */
@Component
class AdminUsuariasAdapter implements UsuariasPort {

    private final IdentityApi identityApi;

    AdminUsuariasAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<UserSummary> find(Long userId) {
        return identityApi.findUser(userId);
    }

    @Override
    public Optional<UserSummary> conCorreo(String correo) {
        return identityApi.findUserByEmail(correo);
    }

    @Override
    public Map<Long, UserSummary> porId() {
        return identityApi.usersById();
    }

    @Override
    public Map<Long, UserSummary> porCuenta() {
        return identityApi.usersByAccountId();
    }
}
