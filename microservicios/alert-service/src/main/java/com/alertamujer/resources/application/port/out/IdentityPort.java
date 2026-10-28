package com.alertamujer.resources.application.port.out;

/** Output port to identity (Auth Service over HTTP). */
public interface IdentityPort {

    /** The JWT carries users.id; calls are stored by user_profile.id. */
    Long requiredProfile(Long userId);
}
