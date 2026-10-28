package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void aNewPasswordHasBetween8And100Characters() {
        assertThrows(IllegalArgumentException.class, () -> Account.validateNewPassword(null));
        assertThrows(IllegalArgumentException.class, () -> Account.validateNewPassword("1234567"));
        assertThrows(IllegalArgumentException.class, () -> Account.validateNewPassword("x".repeat(101)));
        assertDoesNotThrow(() -> Account.validateNewPassword("12345678"));
    }

    @Test
    void blockedOrInactiveDoesNotRefreshTheSession() {
        Account a = Account.newOne(1L, "hash");
        assertTrue(a.canRefreshSession());

        a.changeStatus("blocked");
        assertTrue(a.blocked());
        assertFalse(a.canRefreshSession());

        a.changeStatus("inactive");
        assertFalse(a.canRefreshSession());
    }

    @Test
    void onlyAcceptsKnownStatuses() {
        assertThrows(IllegalArgumentException.class, () -> Account.newOne(1L, "hash").changeStatus("raro"));
    }

    @Test
    void deactivateReplacesThePasswordWithAnUnusableOne() {
        Account a = Account.newOne(1L, "hash");
        a.deactivate("otro");
        assertEquals("inactive", a.getStatus());
        assertEquals("otro", a.getPasswordHash());
    }
}
