package com.alertamujer.identity.domain.model;

/** Changes by the administration to a person and her account. A null field is left untouched. */
public record AccountChanges(String accountStatus, String firstName, String lastName, String telephone, String email) {
}
