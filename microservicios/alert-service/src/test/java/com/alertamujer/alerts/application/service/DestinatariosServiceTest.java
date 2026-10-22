package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase.ContactoNotificado;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.DestinatarioRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertContact;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinatariosServiceTest {

    @Mock private DestinatarioRepositorio repository;
    @Mock private ContactoRepositorio contactos;
    @Mock private IdentityPort identity;
    @Mock private EventosAlertaPort events;

    @InjectMocks private DestinatariosService service;

    private static Alert alert(String status) {
        Alert a = new Alert();
        a.setId(5L);
        a.setUserProfileId(10L);
        a.setStatus(status);
        return a;
    }

    private static EmergencyContact contacto(Long id, String name, String email) {
        EmergencyContact c = new EmergencyContact();
        c.setId(id);
        c.setContactName(name);
        c.setEmail(email);
        return c;
    }

    @Test
    @SuppressWarnings("unchecked")
    void registrar_creaCorreoYPushSegunElContacto() {
        when(contactos.ofUser(10L)).thenReturn(List.of(
                contacto(1L, "Con app", "app@correo.com"),
                contacto(2L, "Solo correo", "otra@correo.com"),
                contacto(3L, "Sin correo", null)));
        when(identity.tieneCuenta(anyString())).thenReturn(false);
        when(identity.tieneCuenta("app@correo.com")).thenReturn(true);
        Alert activa = alert("active");

        service.registrar(activa, null);

        ArgumentCaptor<List<AlertContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).guardarTodos(captor.capture());
        List<AlertContact> records = captor.getValue();
        assertEquals(3, records.size());
        assertEquals("email", records.get(0).getChannel());
        assertEquals("push", records.get(1).getChannel());
        assertEquals(1L, records.get(1).getEmergencyContactId());
        assertEquals(2L, records.get(2).getEmergencyContactId());
        assertEquals("pending", records.get(2).getStatus());
        verify(events).alertaActivada(activa);
    }

    @Test
    @SuppressWarnings("unchecked")
    void registrar_alertaIndividualSoloAvisaAlContactoElegido() {
        when(contactos.ofUser(10L)).thenReturn(List.of(
                contacto(1L, "Eva", "eva@correo.com"), contacto(2L, "Sara", "sara@correo.com")));

        service.registrar(alert("active"), List.of(2L));

        ArgumentCaptor<List<AlertContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).guardarTodos(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(2L, captor.getValue().get(0).getEmergencyContactId());
    }

    @Test
    void registrar_unContactoAjenoNoAvisaANadie() {
        when(contactos.ofUser(10L)).thenReturn(List.of(contacto(1L, "Eva", "eva@correo.com")));

        assertThrows(IllegalArgumentException.class, () -> service.registrar(alert("active"), List.of(99L)));
        verifyNoInteractions(repository, events);
    }

    @Test
    void contactosDe_unEnvioAbiertoDeUnaAlertaTerminadaQuedaVencido() {
        EmergencyContact eva = contacto(1L, "Eva", "eva@correo.com");
        eva.setRelationship("Hermana");
        when(contactos.ofUser(10L)).thenReturn(List.of(eva));
        AlertContact email = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        AlertContact borrado = AlertContact.created(5L, null, "email", "vieja@correo.com");
        borrado.setStatus("failed");
        when(repository.ofAlert(5L)).thenReturn(List.of(email, borrado));

        List<ContactoNotificado> resultado = service.contactosDe(alert("cancelled"));

        assertEquals(2, resultado.size());
        assertEquals("Eva", resultado.get(0).name());
        assertEquals("Hermana", resultado.get(0).relationship());
        assertEquals("expired", resultado.get(0).channels().get(0).status());
        assertEquals("Contacto eliminado", resultado.get(1).name());
        assertEquals("failed", resultado.get(1).channels().get(0).status());
    }

    @Test
    void destinatarios_incluyenElNombreDelContacto() {
        AlertContact envio = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        envio.setId(40L);
        when(repository.ofAlert(5L)).thenReturn(List.of(envio));
        when(contactos.buscarVarios(Set.of(1L))).thenReturn(List.of(contacto(1L, "Eva", "eva@correo.com")));

        assertEquals(List.of(new AlertRecipient(40L, 1L, "Eva", "email", "eva@correo.com")), service.destinatarios(5L));
    }

    @Test
    void registrarEntrega_guardaElResultado() {
        AlertContact envio = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        when(repository.find(40L)).thenReturn(Optional.of(envio));

        service.registrarEntrega(40L, "sent", null);

        assertEquals("sent", envio.getStatus());
        assertEquals(1, envio.getAttempts());
        verify(repository).save(envio);
    }

    @Test
    void alertasAvisadasA_sinCorreoNoConsulta() {
        assertEquals(List.of(), service.alertasAvisadasA(" "));
        verify(repository, never()).alertasAvisadasA(any());

        when(repository.alertasAvisadasA("eva@correo.com")).thenReturn(List.of(5L));
        assertEquals(List.of(5L), service.alertasAvisadasA(" eva@correo.com "));
    }

    @Test
    void contactosDe_unaAlertaActivaMuestraElEstadoReal() {
        when(contactos.ofUser(10L)).thenReturn(List.of());
        AlertContact envio = AlertContact.created(5L, 1L, "push", "App");
        envio.setSentAt(LocalDateTime.now());
        when(repository.ofAlert(5L)).thenReturn(List.of(envio));

        List<ContactoNotificado> resultado = service.contactosDe(alert("active"));

        assertEquals("Contacto", resultado.get(0).name());
        assertEquals("pending", resultado.get(0).channels().get(0).status());
    }
}
