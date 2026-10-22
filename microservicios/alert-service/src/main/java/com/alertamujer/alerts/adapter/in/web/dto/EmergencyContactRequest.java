package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.ContactData;

/** Body to create or change a contact. photo: image data URI; "" removes it. */
public record EmergencyContactRequest(String contactName, String telephone, String email, String relationship,
                                      String photo) {

    public ContactData toData() {
        return new ContactData(contactName, telephone, email, relationship, photo);
    }
}
