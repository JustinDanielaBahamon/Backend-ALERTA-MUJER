package com.alertamujer.evidence.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/**
 * Output port: user data. Today the identity module answers it in memory; once the microservices are
 * split, an HTTP adapter to identity-service.
 */
public interface IdentityPort {

    /** The JWT carries users.id; alerts belong to user_profile.id. */
    Long profileOf(Long userId);

    Map<Long, UserSummary> usersByProfile();

    Optional<UserSummary> userOfProfile(Long userProfileId);
}
