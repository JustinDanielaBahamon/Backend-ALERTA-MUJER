package com.alertamujer.alerts.domain.model;

/** Datos de un contacto de emergencia. Al actualizar, un campo null se deja como estaba. */
public record ContactData(String contactName, String telephone, String email, String relationship, String photo) {
}
