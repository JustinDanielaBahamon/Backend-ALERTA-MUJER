package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyContactTest {

    private static final String PHOTO = "data:image/jpeg;base64,/9j/4AAQ";

    @Test
    void updateOnlyChangesTheSentFields() {
        EmergencyContact c = EmergencyContact.created(10L, new ContactData("Eva", "300", "eva@correo.com", "Hermana", null));

        c.update(new ContactData(null, "311", " nueva@correo.com ", null, null));

        assertEquals("Eva", c.getContactName());
        assertEquals("311", c.getTelephone());
        assertEquals("nueva@correo.com", c.getEmail());
        assertEquals("Hermana", c.getRelationship());

        c.update(new ContactData(null, null, " ", null, null));
        assertNull(c.getEmail());
        assertEquals("", c.emailToNotify());
    }

    @Test
    void savesAndRemovesThePhoto() {
        EmergencyContact c = EmergencyContact.created(10L, new ContactData("Eva", null, null, null, null));

        c.update(new ContactData(null, null, null, null, PHOTO));
        assertEquals(PHOTO, c.getPhoto());
        c.update(new ContactData(null, null, null, null, null));
        assertEquals(PHOTO, c.getPhoto());
        c.update(new ContactData(null, null, null, null, ""));
        assertNull(c.getPhoto());
    }

    @Test
    void rejectsAPhotoThatIsNotAnImage() {
        assertThrows(IllegalArgumentException.class,
                () -> EmergencyContact.created(10L, new ContactData("Eva", null, null, null, "<script>")));
    }
}
