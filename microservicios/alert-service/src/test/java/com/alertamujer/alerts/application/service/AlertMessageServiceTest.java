package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import com.alertamujer.alerts.infrastructure.repository.AlertMessageRepository;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertMessageServiceTest {

    @Mock private AlertMessageRepository repository;
    @Mock private AlertRepository alertRepository;
    @Mock private IdentityApi identityApi;
    @InjectMocks private AlertMessageService service;

    private Alert alerta(String estado) {
        Alert a = new Alert();
        a.setId(5L);
        a.setUserProfileId(10L);
        a.setStatus(estado);
        return a;
    }

    private void guardaTalCual() {
        lenient().when(repository.save(any(AlertMessage.class))).thenAnswer(inv -> {
            AlertMessage m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
    }

    @Test
    void laDuenaEscribeEnSuAlertaActiva() {
        when(alertRepository.findById(5L)).thenReturn(Optional.of(alerta("active")));
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(10L));
        when(identityApi.findUser(1L)).thenReturn(Optional.of(new UserSummary(1L, "Laura", "G", "l@c.co", null, 1L, null)));
        guardaTalCual();

        var m = service.enviarDeLaDuena(5L, 1L, "  No puedo hablar  ");

        assertEquals("owner", m.senderType());
        assertEquals("Laura", m.senderName());
        assertEquals("No puedo hablar", m.body());
    }

    @Test
    void unaAlertaAjenaSeRespondeComoInexistente() {
        when(alertRepository.findById(5L)).thenReturn(Optional.of(alerta("active")));
        when(identityApi.findProfileId(2L)).thenReturn(Optional.of(99L));

        assertThrows(ResourceNotFoundException.class, () -> service.enviarDeLaDuena(5L, 2L, "hola"));
        assertThrows(ResourceNotFoundException.class, () -> service.mensajesDeLaDuena(5L, 2L, 0L));
        verify(repository, never()).save(any());
    }

    @Test
    void conLaAlertaTerminadaOMensajesInvalidosNoSeGuardaNada() {
        when(alertRepository.findById(5L)).thenReturn(Optional.of(alerta("cancelled")));
        assertThrows(IllegalArgumentException.class, () -> service.enviarDeContacto(5L, "Ana", "Voy en camino"));

        when(alertRepository.findById(6L)).thenReturn(Optional.of(alertaConId(6L)));
        assertThrows(IllegalArgumentException.class, () -> service.enviarDeContacto(6L, "Ana", "   "));
        assertThrows(IllegalArgumentException.class, () -> service.enviarDeContacto(6L, "Ana", "x".repeat(501)));
        verify(repository, never()).save(any());
    }

    @Test
    void elContactoSinNombreApareceComoContacto() {
        when(alertRepository.findById(6L)).thenReturn(Optional.of(alertaConId(6L)));
        guardaTalCual();

        var m = service.enviarDeContacto(6L, "  ", "Ya llamé a la policía");

        assertEquals("contact", m.senderType());
        assertEquals("Contacto", m.senderName());
    }

    private Alert alertaConId(Long id) {
        Alert a = alerta("active");
        a.setId(id);
        return a;
    }
}
