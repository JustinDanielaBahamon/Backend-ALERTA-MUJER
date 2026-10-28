package com.alertamujer.identity.api;

/**
 * An account was deleted (by the user herself or with the approval of two admins). The other
 * services erase the personal data they keep about her. userProfileId may be null if she had
 * no profile.
 */
public record AccountDeletedEvent(Long userId, Long userProfileId) {}
