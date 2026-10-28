package com.alertamujer.notifications.application.port.out;

import java.util.Optional;

/** Output port to identity (Auth Service over HTTP). */
public interface IdentityPort {

    /** Full name of the profile owner (for the emails). */
    Optional<String> profileFullName(Long profile);

    /** Only the first name: the tracking may be forwarded. */
    Optional<String> profileFirstName(Long profile);

    /** users.id of the person with that email, if she has an account. */
    Optional<Long> userWithEmail(String email);

    Optional<String> emailOf(Long userId);

    Optional<Long> profileOf(Long userId);
}
