package com.alertamujer.identity.adapter.in.module;

import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.application.port.in.QueryIdentityUseCase;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityApiAdapterTest {

    private final QueryIdentityUseCase queries = mock(QueryIdentityUseCase.class);
    private final IdentityApiAdapter api = new IdentityApiAdapter(queries);

    @Test
    void requireProfileIdReturnsTheProfileOrFails() {
        when(queries.profileOf(1L)).thenReturn(Optional.of(10L));
        when(queries.profileOf(2L)).thenReturn(Optional.empty());

        assertEquals(10L, api.requireProfileId(1L));
        assertThrows(ResourceNotFoundException.class, () -> api.requireProfileId(2L));
    }

    @Test
    void summarizesWithoutExposingTheModel() {
        User u = User.newOne("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);
        u.setId(1L);
        when(queries.byProfileId()).thenReturn(Map.of(10L, u));

        UserSummary s = api.usersByProfileId().get(10L);

        assertEquals(new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", "3001234567", User.USER_ROLE, u.getCreatedAt()), s);
    }
}
