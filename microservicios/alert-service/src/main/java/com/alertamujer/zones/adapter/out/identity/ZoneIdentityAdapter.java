package com.alertamujer.zones.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.zones.application.port.out.IdentityPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida hacia identity: en Alert Service, IdentityApi es un cliente HTTP a Auth Service. */
@Component
class ZoneIdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    ZoneIdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Long requiredProfile(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    @Override
    public Optional<Long> profileOf(Long userId) {
        return identityApi.findProfileId(userId);
    }
}
