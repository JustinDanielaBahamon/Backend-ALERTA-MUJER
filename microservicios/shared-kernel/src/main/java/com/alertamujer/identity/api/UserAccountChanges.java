package com.alertamujer.identity.api;

/** Changes the administration can make to an account; null fields are not modified. */
public record UserAccountChanges(String accountStatus, String firstName, String lastName, String telephone,
                                 String email) {}
