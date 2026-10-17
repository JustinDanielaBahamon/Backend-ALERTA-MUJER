package com.alertamujer.shared.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FotoDataUriTest {

    @Test
    void aceptaUnaImagenEnBase64() {
        String foto = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
        assertEquals(foto, FotoDataUri.validar(foto));
    }

    @Test
    void vaciaQuitaLaFoto() {
        assertNull(FotoDataUri.validar(""));
        assertNull(FotoDataUri.validar(null));
    }

    @Test
    void rechazaLoQueNoEsImagen() {
        assertThrows(IllegalArgumentException.class, () -> FotoDataUri.validar("data:text/html;base64,PGgxPg=="));
        assertThrows(IllegalArgumentException.class, () -> FotoDataUri.validar("https://ejemplo.com/foto.jpg"));
    }

    @Test
    void rechazaFotosDemasiadoGrandes() {
        String grande = "data:image/jpeg;base64," + "A".repeat(FotoDataUri.MAXIMO_CARACTERES);
        assertThrows(IllegalArgumentException.class, () -> FotoDataUri.validar(grande));
    }
}
