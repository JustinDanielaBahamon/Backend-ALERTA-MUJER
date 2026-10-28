package com.alertamujer.identity.api;

import java.util.Map;
import java.util.Optional;

/**
 * Queries of the identity module for the other modules. Input ids are always users.id,
 * unless the method name says otherwise (profile = user_profile.id, account = account.id).
 */
public interface IdentityApi {

    /** The user's app profile; throws ResourceNotFoundException if she has none. */
    Long requireProfileId(Long userId);

    Optional<Long> findProfileId(Long userId);

    /** The person's account (credentials); throws ResourceNotFoundException if she has none. */
    Long requireAccountId(Long userId);

    Optional<UserSummary> findUser(Long userId);

    Optional<UserSummary> findUserByEmail(String email);

    Optional<UserSummary> findUserByProfileId(Long profileId);

    Optional<UserSummary> findUserByAccountId(Long accountId);

    /** All people, by users.id (panel lists). */
    Map<Long, UserSummary> usersById();

    /** All people with an app profile, by user_profile.id. */
    Map<Long, UserSummary> usersByProfileId();

    /** All people with an account, by account.id. */
    Map<Long, UserSummary> usersByAccountId();
}
