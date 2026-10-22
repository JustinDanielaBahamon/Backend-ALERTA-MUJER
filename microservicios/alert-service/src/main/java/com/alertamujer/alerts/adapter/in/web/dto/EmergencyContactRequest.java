package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.ContactData;

/** Body para crear o modificar un contacto. photo: data URI de imagen; "" la quita. */
public record EmergencyContactRequest(String contactName, String telephone, String email, String relationship,
                                      String photo) {

    public ContactData toData() {
        return new ContactData(contactName, telephone, email, relationship, photo);
    }
}
