package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.domain.model.CambiosUsuaria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Traducción del PUT /api/users/me (claves ausentes, con null o con valor) a los cambios del dominio. */
class UserControllerTest {

    @Test
    void distingueLaClaveAusenteDeLaEnviadaConNull() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Ana");
        body.put("telephone", null);
        body.put("birthdate", "1990-01-02");

        CambiosUsuaria c = UserController.cambios(body);

        assertEquals(Optional.of("Ana"), c.firstName());
        assertEquals(Optional.empty(), c.telephone());
        assertNull(c.lastName());
        assertNull(c.profilePhoto());
        assertEquals(LocalDate.of(1990, 1, 2), c.birthdate());
    }

    @Test
    void unaFechaConOtroFormatoSeIgnora() {
        assertNull(UserController.cambios(Map.of("birthdate", "02/01/1990")).birthdate());
    }
}
