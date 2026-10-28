package com.alertamujer.evidence.adapter.out.identity;

import com.alertamujer.evidence.application.port.out.IdentityPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Output adapter to the identity module, in memory; later, an HTTP client to identity-service. */
@Component
class IdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    IdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Long profileOf(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    @Override
    public Map<Long, UserSummary> usersByProfile() {
        return identityApi.usersByProfileId();
    }

    @Override
    public Optional<UserSummary> userOfProfile(Long userProfileId) {
        return identityApi.findUserByProfileId(userProfileId);
    }
}
