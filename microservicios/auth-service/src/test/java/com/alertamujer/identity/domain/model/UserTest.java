package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la persona registrada, sin Spring ni base de datos. */
class UserTest {

    @Test
    void unaUsuariaNuevaGuardaElTelefonoLimpioYLosVaciosComoNull() {
        User u = User.nueva("Ana Gómez", "ana@correo.com", " 300 123-4567 ", " ", "123", LocalDate.of(1995, 5, 5));

        assertEquals("Ana", u.getFirstName());
        assertEquals("Gómez", u.getLastName());
        assertEquals("3001234567", u.getTelephone());
        assertNull(u.getDocumentType());
        assertEquals("123", u.getDocumentNumber());
        assertEquals(User.ROL_USUARIA, u.getRoleId());
        assertFalse(u.esAdmin());
        assertEquals("user", u.rolDelToken());
    }

    @Test
    void sinApellidoQuedaVacio() {
        assertEquals("", User.nueva("Ana", "ana@correo.com", null, null, null, null).getLastName());
    }

    @Test
    void laFechaDelFormularioEsDdMmAaaaYUnaInvalidaSeIgnora() {
        assertEquals(LocalDate.of(1995, 5, 5), User.fechaDeFormulario("05/05/1995"));
        assertNull(User.fechaDeFormulario("31/02/1995"));
        assertNull(User.fechaDeFormulario("1995-05-05"));
        assertNull(User.fechaDeFormulario(" "));
    }

    @Test
    void desdeGoogleUsaLosNombresDeGoogleOElCorreo() {
        User conNombres = User.desdeGoogle("eva@correo.com", "Eva María Ruiz", "Eva", "Ruiz");
        assertEquals("Eva", conNombres.getFirstName());
        assertEquals("Ruiz", conNombres.getLastName());

        User soloNombre = User.desdeGoogle("eva@correo.com", "Eva María Ruiz", null, null);
        assertEquals("Eva", soloNombre.getFirstName());
        assertEquals("María Ruiz", soloNombre.getLastName());

        User sinNada = User.desdeGoogle("eva@correo.com", null, null, null);
        assertEquals("eva", sinNada.getFirstName());
        assertEquals("", sinNada.getLastName());
    }

    @Test
    void actualizarDatosSoloTocaLoEnviado() {
        User u = User.nueva("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);

        u.actualizarDatos(new CambiosUsuaria(Optional.of("Ana María"), null, Optional.empty(), null, null,
                LocalDate.of(1990, 1, 2), null));

        assertEquals("Ana María", u.getFirstName());
        assertEquals("Gómez", u.getLastName());
        assertNull(u.getTelephone());
        assertEquals("123", u.getDocumentNumber());
        assertEquals(LocalDate.of(1990, 1, 2), u.getBirthdate());
    }

    @Test
    void anonimizarBorraLosDatosPersonales() {
        User u = User.nueva("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", LocalDate.of(1995, 5, 5));
        u.setId(7L);

        u.anonimizar();

        assertEquals("Cuenta eliminada", u.nombreCompleto());
        assertEquals("eliminada-7@alertamujer.invalid", u.getEmail());
        assertNull(u.getTelephone());
        assertNull(u.getDocumentNumber());
        assertNull(u.getBirthdate());
    }

    @Test
    void elRolDosEsAdministradora() {
        User u = new User();
        u.setRoleId(User.ADMIN_ROLE);
        assertTrue(u.esAdmin());
        assertEquals("admin", u.rolDelToken());
    }
}
