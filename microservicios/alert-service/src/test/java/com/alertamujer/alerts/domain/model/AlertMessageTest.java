package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertMessageTest {

    private static Alert alerta(String estado) {
        Alert a = new Alert();
        a.setId(5L);
        a.setStatus(estado);
        return a;
    }

    @Test
    void escribeElMensajeLimpio() {
        AlertMessage m = AlertMessage.escribir(alerta("active"), AlertMessage.DE_LA_DUENA, " Ana ", "  Estoy bien  ", 0);

        assertEquals(5L, m.getAlertId());
        assertEquals("owner", m.getSenderType());
        assertEquals("Ana", m.getSenderName());
        assertEquals("Estoy bien", m.getBody());
    }

    @Test
    void sinNombreUsaUnoPorDefectoYCortaLosLargos() {
        assertEquals("Ella", AlertMessage.escribir(alerta("active"), AlertMessage.DE_LA_DUENA, null, "Hola", 0).getSenderName());
        assertEquals("Contacto", AlertMessage.escribir(alerta("active"), AlertMessage.DE_CONTACTO, "  ", "Hola", 0).getSenderName());
        assertEquals(AlertMessage.MAXIMO_NOMBRE,
                AlertMessage.escribir(alerta("active"), AlertMessage.DE_CONTACTO, "x".repeat(100), "Hola", 0).getSenderName().length());
    }

    @Test
    void rechazaMensajesInvalidosOConLaAlertaTerminada() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.escribir(alerta("resolved"), AlertMessage.DE_CONTACTO, "Eva", "Hola", 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.escribir(alerta("active"), AlertMessage.DE_CONTACTO, "Eva", "   ", 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.escribir(alerta("active"), AlertMessage.DE_CONTACTO, "Eva", "x".repeat(501), 0));
        assertThrows(IllegalArgumentException.class,
                () -> AlertMessage.escribir(alerta("active"), AlertMessage.DE_CONTACTO, "Eva", "Hola", AlertMessage.MAXIMO_POR_ALERTA));
    }
}
