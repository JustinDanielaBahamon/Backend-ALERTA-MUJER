package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.domain.model.UserChanges;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Translation of PUT /api/users/me (missing keys, null or with a value) to the domain changes. */
class UserControllerTest {

    @Test
    void distinguishesAMissingKeyFromOneSentAsNull() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Ana");
        body.put("telephone", null);
        body.put("birthdate", "1990-01-02");

        UserChanges c = UserController.changes(body);

        assertEquals(Optional.of("Ana"), c.firstName());
        assertEquals(Optional.empty(), c.telephone());
        assertNull(c.lastName());
        assertNull(c.profilePhoto());
        assertEquals(LocalDate.of(1990, 1, 2), c.birthdate());
    }

    @Test
    void aDateInAnotherFormatIsIgnored() {
        assertNull(UserController.changes(Map.of("birthdate", "02/01/1990")).birthdate());
    }
}
