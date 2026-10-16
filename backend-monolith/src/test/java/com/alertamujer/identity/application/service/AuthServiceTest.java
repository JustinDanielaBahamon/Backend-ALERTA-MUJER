package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RoleRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher events;

    @Mock
    private com.alertamujer.identity.infrastructure.config.JwtKeyProvider jwtKeyProvider;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Account testAccount;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Set JWT secret for testing
        ReflectionTestUtils.setField(authService, "jwtKey", 
            io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                "clave-solo-para-pruebas-unitarias-de-auth-0123456789".getBytes()
            ));

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("user");
        userRole.setDescription("Usuario estándar");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Ana");
        testUser.setLastName("García");
        testUser.setEmail("ana@test.com");
        testUser.setTelephone("3001234567");
        testUser.setRole(userRole);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setPasswordHash(passwordEncoder.encode("Test123"));
        testAccount.setStatus("active");
        testAccount.setLastAccess(LocalDateTime.now());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(userProfileRepository.existsByUserId(anyLong())).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

        // Act
        User result = authService.register("Ana García", "ana@test.com", "Test123", "3001234567");

        // Assert
        assertNotNull(result);
        assertEquals("Ana", result.getFirstName());
        assertEquals("García", result.getLastName());
        assertEquals("ana@test.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.register("Ana García", "ana@test.com", "Test123", "3001234567")
        );
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Map<String, Object> result = authService.login("ana@test.com", "Test123");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("token"));
        assertTrue(result.containsKey("user"));
        assertNotNull(result.get("token"));
        assertEquals(testUser, result.get("user"));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testLogin_InvalidEmail() {
        // Arrange
        when(userRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login("invalid@test.com", "Test123")
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login("ana@test.com", "WrongPassword")
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLogin_BlockedAccount() {
        // Arrange
        testAccount.setStatus("blocked");
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login("ana@test.com", "Test123")
        );
        assertEquals("Account blocked", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testGenerateTokenForUser() {
        // Act
        String token = authService.generateTokenForUser(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void changePassword_exigeLaActualYGuardaLaNueva() {
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(1L, "Otra123", "NuevaClave2026"));
        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(1L, "Test123", "corta"));

        authService.changePassword(1L, "Test123", "NuevaClave2026");

        assertTrue(new BCryptPasswordEncoder().matches("NuevaClave2026", testAccount.getPasswordHash()));
        verify(accountRepository).save(testAccount);
    }

    @Test
    void login_deAdminRegistraEventosDeSesion() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        testUser.setRole(adminRole);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(Exception.class, () -> authService.login("ana@test.com", "mala"));
        authService.login("ana@test.com", "Test123");

        verify(events).publishEvent(new com.alertamujer.identity.api.AdminSessionEvent(1L, "LOGIN_FAILED", "Contraseña incorrecta"));
        verify(events).publishEvent(new com.alertamujer.identity.api.AdminSessionEvent(1L, "LOGIN_SUCCESS", "Con correo y contraseña"));
    }

    @Test
    void login_deUsuariaNoLlenaLaAuditoria() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        authService.login("ana@test.com", "Test123");

        verifyNoInteractions(events);
    }

    @Test
    void refreshToken_entregaUnTokenNuevo() {
        String refresh = ReflectionTestUtils.invokeMethod(authService, "generateRefreshToken", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        assertNotNull(authService.refreshToken(refresh).get("token"));
    }

    @Test
    void refreshToken_cuentaBloqueadaNoRenueva() {
        String refresh = ReflectionTestUtils.invokeMethod(authService, "generateRefreshToken", 1L);
        testAccount.setStatus("blocked");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(com.alertamujer.shared.exception.InvalidCredentialsException.class,
                () -> authService.refreshToken(refresh));
    }
}
