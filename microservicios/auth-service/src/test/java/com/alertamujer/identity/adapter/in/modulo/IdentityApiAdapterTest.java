package com.alertamujer.identity.adapter.in.modulo;

import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.application.port.in.ConsultarIdentidadUseCase;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityApiAdapterTest {

    private final ConsultarIdentidadUseCase consultas = mock(ConsultarIdentidadUseCase.class);
    private final IdentityApiAdapter api = new IdentityApiAdapter(consultas);

    @Test
    void requireProfileIdDevuelveElPerfilOFalla() {
        when(consultas.perfilDe(1L)).thenReturn(Optional.of(10L));
        when(consultas.perfilDe(2L)).thenReturn(Optional.empty());

        assertEquals(10L, api.requireProfileId(1L));
        assertThrows(ResourceNotFoundException.class, () -> api.requireProfileId(2L));
    }

    @Test
    void resumeSinExponerElModelo() {
        User u = User.nueva("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);
        u.setId(1L);
        when(consultas.porIdDePerfil()).thenReturn(Map.of(10L, u));

        UserSummary s = api.usersByProfileId().get(10L);

        assertEquals(new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", "3001234567", User.ROL_USUARIA, u.getCreatedAt()), s);
    }
}
