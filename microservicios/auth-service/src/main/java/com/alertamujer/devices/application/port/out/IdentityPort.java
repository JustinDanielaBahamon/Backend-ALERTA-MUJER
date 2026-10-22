package com.alertamujer.devices.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Output port to identity: devices are stored by account.id. */
public interface IdentityPort {

    /** account.id of the person (users.id), if she has an account. */
    Optional<Long> accountOf(Long userId);

    /** Like accountOf, but fails with ResourceNotFoundException if she has no account. */
    Long requiredAccount(Long userId);

    /** users.id of the profile owner. */
    Optional<Long> userOfProfile(Long profile);

    Optional<UserSummary> accountOwner(Long accountId);

    Map<Long, UserSummary> ownersByAccount();
}
