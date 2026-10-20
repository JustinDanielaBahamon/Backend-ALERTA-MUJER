package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void unaContrasenaNuevaTieneEntre8Y100Caracteres() {
        assertThrows(IllegalArgumentException.class, () -> Account.validarContrasenaNueva(null));
        assertThrows(IllegalArgumentException.class, () -> Account.validarContrasenaNueva("1234567"));
        assertThrows(IllegalArgumentException.class, () -> Account.validarContrasenaNueva("x".repeat(101)));
        assertDoesNotThrow(() -> Account.validarContrasenaNueva("12345678"));
    }

    @Test
    void bloqueadaOInactivaNoRenuevaLaSesion() {
        Account a = Account.nueva(1L, "hash");
        assertTrue(a.puedeRenovarSesion());

        a.cambiarEstado("blocked");
        assertTrue(a.bloqueada());
        assertFalse(a.puedeRenovarSesion());

        a.cambiarEstado("inactive");
        assertFalse(a.puedeRenovarSesion());
    }

    @Test
    void soloAceptaEstadosConocidos() {
        assertThrows(IllegalArgumentException.class, () -> Account.nueva(1L, "hash").cambiarEstado("raro"));
    }

    @Test
    void desactivarCambiaLaContrasenaPorUnaInutilizable() {
        Account a = Account.nueva(1L, "hash");
        a.desactivar("otro");
        assertEquals("inactive", a.getStatus());
        assertEquals("otro", a.getPasswordHash());
    }
}
