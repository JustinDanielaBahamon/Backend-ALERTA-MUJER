package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityApiServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private IdentityApiService api;

    private User admin() {
        Role rol = new Role();
        rol.setId(2L);
        User u = new User();
        u.setId(7L);
        u.setFirstName("Ana");
        u.setLastName("Gómez");
        u.setEmail("ana@test.local");
        u.setRole(rol);
        return u;
    }

    @Test
    void requireProfileId_devuelveElPerfilOFalla() {
        UserProfile p = new UserProfile();
        p.setId(10L);
        when(userProfileRepository.findByUserId(7L)).thenReturn(Optional.of(p));
        when(userProfileRepository.findByUserId(8L)).thenReturn(Optional.empty());

        assertEquals(10L, api.requireProfileId(7L));
        assertThrows(ResourceNotFoundException.class, () -> api.requireProfileId(8L));
    }

    @Test
    void usersByProfileId_resumeSinExponerLaEntidad() {
        when(userRepository.findAll()).thenReturn(List.of(admin()));
        UserProfile p = new UserProfile();
        p.setId(10L);
        p.setUserId(7L);
        when(userProfileRepository.findAll()).thenReturn(List.of(p));

        Map<Long, UserSummary> porPerfil = api.usersByProfileId();

        UserSummary s = porPerfil.get(10L);
        assertEquals("Ana Gómez", s.fullName());
        assertTrue(s.admin());
    }

    @Test
    void findUserByAccountId_pasaPorLaCuenta() {
        Account a = new Account();
        a.setId(39L);
        a.setUserId(7L);
        when(accountRepository.findById(39L)).thenReturn(Optional.of(a));
        when(userRepository.findById(7L)).thenReturn(Optional.of(admin()));

        assertEquals("ana@test.local", api.findUserByAccountId(39L).orElseThrow().email());
    }
}
