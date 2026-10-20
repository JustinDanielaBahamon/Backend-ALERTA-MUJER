package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RecoveryRequestRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private RecoveryRequestRepository recoveryRepository;
    @Mock private ApplicationEventPublisher events;

    private PasswordRecoveryService service;
    private Account cuenta;
    private final List<RecoveryRequest> guardadas = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new PasswordRecoveryService(userRepository, accountRepository, recoveryRepository, events);
        User user = new User();
        user.setId(1L);
        user.setFirstName("Ana");
        user.setLastName("Gómez");
        user.setEmail("ana@test.com");
        cuenta = new Account();
        cuenta.setId(5L);
        cuenta.setUser(user);
        cuenta.setStatus("active");
        cuenta.setPasswordHash(new BCryptPasswordEncoder().encode("Anterior123"));
        lenient().when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        lenient().when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(cuenta));
        lenient().when(accountRepository.findById(5L)).thenReturn(Optional.of(cuenta));
        lenient().when(recoveryRepository.save(any())).thenAnswer(inv -> {
            RecoveryRequest r = inv.getArgument(0);
            if (!guardadas.contains(r)) guardadas.add(r);
            return r;
        });
        lenient().when(recoveryRepository.findByAccountIdAndStatus(eq(5L), eq("pending")))
                .thenAnswer(inv -> guardadas.stream().filter(r -> "pending".equals(r.getStatus())).toList());
    }

    private static <T> T eq(T v) {
        return org.mockito.ArgumentMatchers.eq(v);
    }

    private String pedirCodigo() {
        service.solicitar("ana@test.com", "1.1.1.1");
        ArgumentCaptor<PasswordRecoveryRequestedEvent> evento = ArgumentCaptor.forClass(PasswordRecoveryRequestedEvent.class);
        verify(events).publishEvent(evento.capture());
        return evento.getValue().codigo();
    }

    @Test
    void enviaUnCodigoDeSeisDigitosYGuardaSoloSuHash() {
        String codigo = pedirCodigo();

        assertTrue(codigo.matches("\\d{6}"));
        assertEquals(1, guardadas.size());
        assertNotEquals(codigo, guardadas.get(0).getTokenHash());
        assertTrue(guardadas.get(0).getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14)));
    }

    @Test
    void correoNoRegistradoNoRevelaNada() {
        when(userRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        service.solicitar("nadie@test.com", null);

        verifyNoInteractions(events);
        verify(recoveryRepository, never()).save(any());
    }

    @Test
    void conElCodigoCorrectoCambiaLaContrasenaYNoSePuedeReusar() {
        String codigo = pedirCodigo();

        service.restablecer("ana@test.com", codigo, "NuevaClave123");

        assertTrue(new BCryptPasswordEncoder().matches("NuevaClave123", cuenta.getPasswordHash()));
        assertEquals("used", guardadas.get(0).getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.restablecer("ana@test.com", codigo, "OtraClave123"));
    }

    @Test
    void cincoIntentosFallidosAnulanElCodigo() {
        String codigo = pedirCodigo();
        String malo = codigo.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < PasswordRecoveryService.MAX_INTENTOS; i++) {
            assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@test.com", malo));
        }
        assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@test.com", codigo));
        assertEquals("expired", guardadas.get(0).getStatus());
    }

    @Test
    void codigoVencidoNoSirve() {
        String codigo = pedirCodigo();
        guardadas.get(0).setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@test.com", codigo));
    }

    @Test
    void noEnviaOtroCodigoAntesDeUnMinuto() {
        pedirCodigo();
        guardadas.get(0).setCreatedAt(LocalDateTime.now());

        service.solicitar("ana@test.com", null);

        verify(events, times(1)).publishEvent(any(Object.class));
    }
}
