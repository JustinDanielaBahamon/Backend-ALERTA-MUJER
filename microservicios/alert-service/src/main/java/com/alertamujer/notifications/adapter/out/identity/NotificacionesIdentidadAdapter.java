package com.alertamujer.notifications.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.application.port.out.IdentidadPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida hacia identity: en Alert Service, IdentityApi es un cliente HTTP a Auth Service. */
@Component
class NotificacionesIdentidadAdapter implements IdentidadPort {

    private final IdentityApi identityApi;

    NotificacionesIdentidadAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<String> nombreCompletoDelPerfil(Long perfil) {
        return identityApi.findUserByProfileId(perfil).map(UserSummary::fullName);
    }

    @Override
    public Optional<String> nombreDePilaDelPerfil(Long perfil) {
        return identityApi.findUserByProfileId(perfil).map(u -> u.firstName() == null ? "" : u.firstName().trim());
    }

    @Override
    public Optional<Long> usuariaConCorreo(String correo) {
        return identityApi.findUserByEmail(correo).map(UserSummary::id);
    }

    @Override
    public Optional<String> correoDe(Long userId) {
        return identityApi.findUser(userId).map(UserSummary::email);
    }

    @Override
    public Optional<Long> perfilDe(Long userId) {
        return identityApi.findProfileId(userId);
    }
}
