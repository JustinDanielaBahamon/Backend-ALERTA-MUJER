package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.identity.api.UserAccountChanges;

/** Cambios parciales de una cuenta desde el panel (todos opcionales). */
public record UpdateUserRequest(String accountStatus, String firstName, String lastName, String telephone, String email) {

    public UserAccountChanges toChanges() {
        return new UserAccountChanges(accountStatus, firstName, lastName, telephone, email);
    }
}
