package com.alertamujer.notifications.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida hacia identity: en Alert Service, IdentityApi es un cliente HTTP a Auth Service. */
@Component
class NotificacionesIdentidadAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    NotificacionesIdentidadAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<String> nombreCompletoDelPerfil(Long profile) {
        return identityApi.findUserByProfileId(profile).map(UserSummary::fullName);
    }

    @Override
    public Optional<String> nombreDePilaDelPerfil(Long profile) {
        return identityApi.findUserByProfileId(profile).map(u -> u.firstName() == null ? "" : u.firstName().trim());
    }

    @Override
    public Optional<Long> usuariaConCorreo(String email) {
        return identityApi.findUserByEmail(email).map(UserSummary::id);
    }

    @Override
    public Optional<String> correoDe(Long userId) {
        return identityApi.findUser(userId).map(UserSummary::email);
    }

    @Override
    public Optional<Long> profileOf(Long userId) {
        return identityApi.findProfileId(userId);
    }
}
