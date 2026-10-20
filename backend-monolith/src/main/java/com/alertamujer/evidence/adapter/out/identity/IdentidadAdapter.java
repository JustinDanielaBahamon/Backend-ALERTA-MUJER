package com.alertamujer.evidence.adapter.out.identity;

import com.alertamujer.evidence.application.port.out.IdentidadPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia el módulo identity, en memoria; luego, cliente HTTP a identity-service. */
@Component
class IdentidadAdapter implements IdentidadPort {

    private final IdentityApi identityApi;

    IdentidadAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Long perfilDe(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    @Override
    public Map<Long, UserSummary> usuariasPorPerfil() {
        return identityApi.usersByProfileId();
    }

    @Override
    public Optional<UserSummary> usuariaDelPerfil(Long userProfileId) {
        return identityApi.findUserByProfileId(userProfileId);
    }
}
