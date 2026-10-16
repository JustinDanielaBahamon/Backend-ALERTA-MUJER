package com.alertamujer.identity.api;

/** Cambios que la administración puede hacer a una cuenta; los campos null no se modifican. */
public record UserAccountChanges(String accountStatus, String firstName, String lastName, String telephone,
                                 String email) {}
