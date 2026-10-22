package com.alertamujer.resources.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.resources.application.port.out.IdentityPort;
import org.springframework.stereotype.Component;

/** Output adapter to identity: in Alert Service, IdentityApi is an HTTP client to Auth Service. */
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
