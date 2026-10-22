package com.alertamujer.resources.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.resources.application.port.out.IdentityPort;
import org.springframework.stereotype.Component;

/** Adaptador de salida hacia identity: en Alert Service, IdentityApi es un cliente HTTP a Auth Service. */
@Component
class ResourceIdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    ResourceIdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Long requiredProfile(Long userId) {
        return identityApi.requireProfileId(userId);
    }
}
