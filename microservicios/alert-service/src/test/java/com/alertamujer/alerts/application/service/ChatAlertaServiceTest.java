package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.MensajeRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatAlertaServiceTest {

    @Mock private MensajeRepositorio mensajes;
    @Mock private AlertaRepositorio alerts;
    @Mock private IdentityPort identidad;

    @InjectMocks private ChatAlertaService service;

    private static Alert alert(String status) {
        Alert a = new Alert();
        a.setId(5L);
        a.setUserProfileId(10L);
        a.setStatus(status);
        return a;
    }

    @Test
    void laDuenaEscribeEnSuAlertaActivaConSuNombre() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert("active")));
        when(identidad.profileOf(1L)).thenReturn(Optional.of(10L));
        when(identidad.usuaria(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", "Gómez", "ana@test.com", null, 1L, null)));
        when(mensajes.save(any(AlertMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertMessage m = service.enviarDeLaDuena(5L, 1L, "Estoy bien");

        assertEquals("owner", m.getSenderType());
        assertEquals("Ana", m.getSenderName());
        assertEquals("Estoy bien", m.getBody());
    }

    @Test
    void unaAlertaAjenaSeRespondeComoInexistente() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert("active")));
        when(identidad.profileOf(2L)).thenReturn(Optional.of(20L));

        assertThrows(ResourceNotFoundException.class, () -> service.mensajesDeLaDuena(5L, 2L, 0L));
        assertThrows(ResourceNotFoundException.class, () -> service.enviarDeLaDuena(5L, 2L, "Hola"));
        verify(mensajes, never()).save(any());
    }

    @Test
    void conLaAlertaTerminadaElContactoNoPuedeEscribir() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert("resolved")));

        assertThrows(IllegalArgumentException.class, () -> service.enviarDeContacto(5L, "Eva", "Hola"));
        verify(mensajes, never()).save(any());
    }

    @Test
    void mensajesSinAfterDevuelveTodos() {
        when(mensajes.posteriores(5L, 0L)).thenReturn(List.of());

        assertEquals(List.of(), service.mensajes(5L, null));
        verify(mensajes).posteriores(5L, 0L);
    }
}
