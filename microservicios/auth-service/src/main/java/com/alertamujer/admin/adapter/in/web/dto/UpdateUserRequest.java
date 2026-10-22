package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.identity.api.UserAccountChanges;

/** Partial changes to an account from the panel (all optional). */
public record UpdateUserRequest(String accountStatus, String firstName, String lastName, String telephone, String email) {

    public UserAccountChanges toChanges() {
        return new UserAccountChanges(accountStatus, firstName, lastName, telephone, email);
    }
}
