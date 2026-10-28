package com.alertamujer.zones.application.port.out;

import java.util.Optional;

/** Output port to identity (Auth Service over HTTP). */
public interface IdentityPort {

    /** The JWT carries users.id; reports are stored by user_profile.id. */
    Long requiredProfile(Long userId);

    Optional<Long> profileOf(Long userId);
}
