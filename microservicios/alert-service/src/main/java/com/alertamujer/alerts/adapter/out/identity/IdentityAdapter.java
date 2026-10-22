package com.alertamujer.alerts.adapter.out.identity;

import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia identity: en Alert Service, IdentityApi es un cliente HTTP a Auth Service. */
@Component
class IdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    IdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<Long> profileOf(Long userId) {
        return identityApi.findProfileId(userId);
    }

    @Override
    public Long perfilRequerido(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    @Override
    public Optional<UserSummary> usuaria(Long userId) {
        return identityApi.findUser(userId);
    }

    @Override
    public Map<Long, UserSummary> usersByProfile() {
        return identityApi.usersByProfileId();
    }

    @Override
    public boolean tieneCuenta(String correo) {
        return identityApi.findUserByEmail(correo).isPresent();
    }

    @Override
    public Optional<Long> usuariaPorCorreo(String correo) {
        return identityApi.findUserByEmail(correo).map(UserSummary::id);
    }
}
