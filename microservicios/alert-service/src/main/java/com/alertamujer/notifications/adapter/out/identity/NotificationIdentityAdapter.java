package com.alertamujer.notifications.adapter.out.identity;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Output adapter to identity: in Alert Service, IdentityApi is an HTTP client to Auth Service. */
@Component
class NotificationIdentityAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    NotificationIdentityAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<String> profileFullName(Long profile) {
        return identityApi.findUserByProfileId(profile).map(UserSummary::fullName);
    }

    @Override
    public Optional<String> profileFirstName(Long profile) {
        return identityApi.findUserByProfileId(profile).map(u -> u.firstName() == null ? "" : u.firstName().trim());
    }

    @Override
    public Optional<Long> userWithEmail(String email) {
        return identityApi.findUserByEmail(email).map(UserSummary::id);
    }

    @Override
    public Optional<String> emailOf(Long userId) {
        return identityApi.findUser(userId).map(UserSummary::email);
    }

    @Override
    public Optional<Long> profileOf(Long userId) {
        return identityApi.findProfileId(userId);
    }
}
