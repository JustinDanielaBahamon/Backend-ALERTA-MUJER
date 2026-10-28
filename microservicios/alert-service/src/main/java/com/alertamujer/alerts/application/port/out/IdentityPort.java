package com.alertamujer.alerts.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Output port: user data (Auth Service). */
public interface IdentityPort {

    /** The JWT carries users.id; alerts, contacts and locations belong to user_profile.id. */
    Optional<Long> profileOf(Long userId);

    /** Like profileOf, but fails if the user has no profile. */
    Long requiredProfile(Long userId);

    Optional<UserSummary> user(Long userId);

    Map<Long, UserSummary> usersByProfile();

    /** true if the email belongs to a registered user (she can also be notified by push). */
    boolean hasAccount(String email);

    /** users.id of the registered person with that email, if any. */
    Optional<Long> userByEmail(String email);
}
