package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertMessageTest {

    private static Alert alert(String status) {
        Alert a = new Alert();
        a.setId(5L);
        a.setStatus(status);
        return a;
    }

    @Test
    void writesTheCleanMessage() {
        AlertMessage m = AlertMessage.write(alert("active"), AlertMessage.FROM_OWNER, " Ana ", "  Estoy bien  ", 0);

        assertEquals(5L, m.getAlertId());
        assertEquals("owner", m.getSenderType());
        assertEquals("Ana", m.getSenderName());
        assertEquals("Estoy bien", m.getBody());
    }

    @Test
    void withoutNameUsesADefaultAndTruncatesLongOnes() {
        assertEquals("Ella", AlertMessage.write(alert("active"), AlertMessage.FROM_OWNER, null, "Hola", 0).getSenderName());
        assertEquals("Contacto", AlertMessage.write(alert("active"), AlertMessage.FROM_CONTACT, "  ", "Hola", 0).getSenderName());
        assertEquals(AlertMessage.MAX_NAME,
                AlertMessage.write(alert("active"), AlertMessage.FROM_CONTACT, "x".repeat(100), "Hola", 0).getSenderName().length());
    }

    @Test
    void rejectsInvalidMessagesOrWithTheAlertEnded() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.write(alert("resolved"), AlertMessage.FROM_CONTACT, "Eva", "Hola", 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.write(alert("active"), AlertMessage.FROM_CONTACT, "Eva", "   ", 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.write(alert("active"), AlertMessage.FROM_CONTACT, "Eva", "x".repeat(501), 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.write(alert("active"), AlertMessage.FROM_CONTACT, "Eva", "Hola", AlertMessage.MAX_PER_ALERT));
    }
}
