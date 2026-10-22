package com.alertamujer.devices.adapter.out.identity;

import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia identity (mismo microservicio) por su contrato IdentityApi. */
@Component
class DeviceIdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    DeviceIdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<Long> accountOf(Long userId) {
        try {
            return Optional.of(identityApi.requireAccountId(userId));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long requiredAccount(Long userId) {
        return identityApi.requireAccountId(userId);
    }

    @Override
    public Optional<Long> userOfProfile(Long profile) {
        return identityApi.findUserByProfileId(profile).map(UserSummary::id);
    }

    @Override
    public Optional<UserSummary> accountOwner(Long accountId) {
        return identityApi.findUserByAccountId(accountId);
    }

    @Override
    public Map<Long, UserSummary> ownersByAccount() {
        return identityApi.usersByAccountId();
    }
}
