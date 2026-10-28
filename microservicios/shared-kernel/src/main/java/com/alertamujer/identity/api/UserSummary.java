package com.alertamujer.identity.api;

import java.time.LocalDateTime;

/** Data about a person that other modules may know (no credentials or documents). */
public record UserSummary(Long id, String firstName, String lastName, String email, String telephone,
                          Long roleId, LocalDateTime createdAt) {

    public static final long ADMIN_ROLE = 2L;

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    public boolean admin() {
        return roleId != null && roleId == ADMIN_ROLE;
    }
}
