package com.alertamujer.alerts.domain.model;

/** Data of an emergency contact. On update, a null field is left as it was. */
public record ContactData(String contactName, String telephone, String email, String relationship, String photo) {
}
