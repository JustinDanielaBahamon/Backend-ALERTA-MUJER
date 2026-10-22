package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyContactTest {

    private static final String FOTO = "data:image/jpeg;base64,/9j/4AAQ";

    @Test
    void actualizarSoloCambiaLosCamposEnviados() {
        EmergencyContact c = EmergencyContact.created(10L, new DatosContacto("Eva", "300", "eva@correo.com", "Hermana", null));

        c.update(new DatosContacto(null, "311", " nueva@correo.com ", null, null));

        assertEquals("Eva", c.getContactName());
        assertEquals("311", c.getTelephone());
        assertEquals("nueva@correo.com", c.getEmail());
        assertEquals("Hermana", c.getRelationship());

        c.update(new DatosContacto(null, null, " ", null, null));
        assertNull(c.getEmail());
        assertEquals("", c.correoParaAvisar());
    }

    @Test
    void guardaYQuitaLaFoto() {
        EmergencyContact c = EmergencyContact.created(10L, new DatosContacto("Eva", null, null, null, null));

        c.update(new DatosContacto(null, null, null, null, FOTO));
        assertEquals(FOTO, c.getPhoto());
        c.update(new DatosContacto(null, null, null, null, null));
        assertEquals(FOTO, c.getPhoto());
        c.update(new DatosContacto(null, null, null, null, ""));
        assertNull(c.getPhoto());
    }

    @Test
    void rechazaUnaFotoQueNoEsImagen() {
        assertThrows(IllegalArgumentException.class,
                () -> EmergencyContact.created(10L, new DatosContacto("Eva", null, null, null, "<script>")));
    }
}
