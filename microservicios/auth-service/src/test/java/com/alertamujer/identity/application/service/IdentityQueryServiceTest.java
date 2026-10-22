package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityQueryServiceTest {

    @Mock private UserRepository users;
    @Mock private ProfileRepository profiles;
    @Mock private AccountRepository accounts;

    @InjectMocks private IdentityQueryService service;

    private static User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void byProfileAndByAccountGoThroughTheirUser() {
        UserProfile profile = UserProfile.of(1L);
        Account account = Account.newOne(1L, "hash");
        when(profiles.find(10L)).thenReturn(Optional.of(profile));
        when(accounts.find(4L)).thenReturn(Optional.of(account));
        when(users.find(1L)).thenReturn(Optional.of(user(1L)));

        assertEquals(1L, service.byProfile(10L).orElseThrow().getId());
        assertEquals(1L, service.byAccount(4L).orElseThrow().getId());
        assertTrue(service.byProfile(null).isEmpty());
        assertTrue(service.user(null).isEmpty());
        assertTrue(service.byEmail(null).isEmpty());
    }

    @Test
    void byProfileIdSkipsProfilesWithoutPerson() {
        UserProfile conPersona = UserProfile.of(1L);
        conPersona.setId(10L);
        UserProfile orphan = UserProfile.of(99L);
        orphan.setId(11L);
        when(users.all()).thenReturn(List.of(user(1L)));
        when(profiles.all()).thenReturn(List.of(conPersona, orphan));

        Map<Long, User> byProfile = service.byProfileId();

        assertEquals(1, byProfile.size());
        assertEquals(1L, byProfile.get(10L).getId());
    }
}
