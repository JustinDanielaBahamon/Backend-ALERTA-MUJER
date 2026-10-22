package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la persona registrada, sin Spring ni base de datos. */
class UserTest {

    @Test
    void aNewUserSavesTheCleanPhoneAndEmptyValuesAsNull() {
        User u = User.newOne("Ana Gómez", "ana@correo.com", " 300 123-4567 ", " ", "123", LocalDate.of(1995, 5, 5));

        assertEquals("Ana", u.getFirstName());
        assertEquals("Gómez", u.getLastName());
        assertEquals("3001234567", u.getTelephone());
        assertNull(u.getDocumentType());
        assertEquals("123", u.getDocumentNumber());
        assertEquals(User.USER_ROLE, u.getRoleId());
        assertFalse(u.esAdmin());
        assertEquals("user", u.roleOfToken());
    }

    @Test
    void withoutLastNameStaysEmpty() {
        assertEquals("", User.newOne("Ana", "ana@correo.com", null, null, null, null).getLastName());
    }

    @Test
    void theFormDateIsDdMmYyyyAndAnInvalidOneIsIgnored() {
        assertEquals(LocalDate.of(1995, 5, 5), User.formDate("05/05/1995"));
        assertNull(User.formDate("31/02/1995"));
        assertNull(User.formDate("1995-05-05"));
        assertNull(User.formDate(" "));
    }

    @Test
    void fromGoogleUsesTheGoogleNamesOrTheEmail() {
        User withNames = User.fromGoogle("eva@correo.com", "Eva María Ruiz", "Eva", "Ruiz");
        assertEquals("Eva", withNames.getFirstName());
        assertEquals("Ruiz", withNames.getLastName());

        User nameOnly = User.fromGoogle("eva@correo.com", "Eva María Ruiz", null, null);
        assertEquals("Eva", nameOnly.getFirstName());
        assertEquals("María Ruiz", nameOnly.getLastName());

        User empty = User.fromGoogle("eva@correo.com", null, null, null);
        assertEquals("eva", empty.getFirstName());
        assertEquals("", empty.getLastName());
    }

    @Test
    void updateDataOnlyTouchesWhatWasSent() {
        User u = User.newOne("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);

        u.updateData(new UserChanges(Optional.of("Ana María"), null, Optional.empty(), null, null,
                LocalDate.of(1990, 1, 2), null));

        assertEquals("Ana María", u.getFirstName());
        assertEquals("Gómez", u.getLastName());
        assertNull(u.getTelephone());
        assertEquals("123", u.getDocumentNumber());
        assertEquals(LocalDate.of(1990, 1, 2), u.getBirthdate());
    }

    @Test
    void anonymizeErasesPersonalData() {
        User u = User.newOne("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", LocalDate.of(1995, 5, 5));
        u.setId(7L);

        u.anonymize();

        assertEquals("Cuenta eliminada", u.fullName());
        assertEquals("eliminada-7@alertamujer.invalid", u.getEmail());
        assertNull(u.getTelephone());
        assertNull(u.getDocumentNumber());
        assertNull(u.getBirthdate());
    }

    @Test
    void roleTwoIsAdmin() {
        User u = new User();
        u.setRoleId(User.ADMIN_ROLE);
        assertTrue(u.esAdmin());
        assertEquals("admin", u.roleOfToken());
    }
}
