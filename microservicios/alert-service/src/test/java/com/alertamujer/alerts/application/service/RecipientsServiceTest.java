package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase.NotifiedContact;
import com.alertamujer.alerts.application.port.out.ContactRepository;
import com.alertamujer.alerts.application.port.out.RecipientRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
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
class RecipientsServiceTest {

    @Mock private RecipientRepository repository;
    @Mock private ContactRepository contacts;
    @Mock private IdentityPort identity;
    @Mock private AlertEventsPort events;

    @InjectMocks private RecipientsService service;

    private static Alert alert(String status) {
        Alert a = new Alert();
        a.setId(5L);
        a.setUserProfileId(10L);
        a.setStatus(status);
        return a;
    }

    private static EmergencyContact contact(Long id, String name, String email) {
        EmergencyContact c = new EmergencyContact();
        c.setId(id);
        c.setContactName(name);
        c.setEmail(email);
        return c;
    }

    @Test
    @SuppressWarnings("unchecked")
    void register_createsEmailAndPushDependingOnTheContact() {
        when(contacts.ofUser(10L)).thenReturn(List.of(
                contact(1L, "Con app", "app@correo.com"),
                contact(2L, "Solo correo", "otra@correo.com"),
                contact(3L, "Sin correo", null)));
        when(identity.hasAccount(anyString())).thenReturn(false);
        when(identity.hasAccount("app@correo.com")).thenReturn(true);
        Alert active = alert("active");

        service.registrar(active, null);

        ArgumentCaptor<List<AlertContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<AlertContact> records = captor.getValue();
        assertEquals(3, records.size());
        assertEquals("email", records.get(0).getChannel());
        assertEquals("push", records.get(1).getChannel());
        assertEquals(1L, records.get(1).getEmergencyContactId());
        assertEquals(2L, records.get(2).getEmergencyContactId());
        assertEquals("pending", records.get(2).getStatus());
        verify(events).alertActivated(active);
    }

    @Test
    @SuppressWarnings("unchecked")
    void register_individualAlertOnlyNotifiesTheChosenContact() {
        when(contacts.ofUser(10L)).thenReturn(List.of(
                contact(1L, "Eva", "eva@correo.com"), contact(2L, "Sara", "sara@correo.com")));

        service.registrar(alert("active"), List.of(2L));

        ArgumentCaptor<List<AlertContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(2L, captor.getValue().get(0).getEmergencyContactId());
    }

    @Test
    void register_someoneElsesContactNotifiesNobody() {
        when(contacts.ofUser(10L)).thenReturn(List.of(contact(1L, "Eva", "eva@correo.com")));

        assertThrows(IllegalArgumentException.class, () -> service.registrar(alert("active"), List.of(99L)));
        verifyNoInteractions(repository, events);
    }

    @Test
    void contactsOf_anOpenDeliveryOfAnEndedAlertIsExpired() {
        EmergencyContact eva = contact(1L, "Eva", "eva@correo.com");
        eva.setRelationship("Hermana");
        when(contacts.ofUser(10L)).thenReturn(List.of(eva));
        AlertContact email = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        AlertContact deletion = AlertContact.created(5L, null, "email", "vieja@correo.com");
        deletion.setStatus("failed");
        when(repository.ofAlert(5L)).thenReturn(List.of(email, deletion));

        List<NotifiedContact> result = service.contactsOf(alert("cancelled"));

        assertEquals(2, result.size());
        assertEquals("Eva", result.get(0).name());
        assertEquals("Hermana", result.get(0).relationship());
        assertEquals("expired", result.get(0).channels().get(0).status());
        assertEquals("Contacto eliminado", result.get(1).name());
        assertEquals("failed", result.get(1).channels().get(0).status());
    }

    @Test
    void recipients_includeTheContactName() {
        AlertContact delivery = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        delivery.setId(40L);
        when(repository.ofAlert(5L)).thenReturn(List.of(delivery));
        when(contacts.findSeveral(Set.of(1L))).thenReturn(List.of(contact(1L, "Eva", "eva@correo.com")));

        assertEquals(List.of(new AlertRecipient(40L, 1L, "Eva", "email", "eva@correo.com")), service.recipients(5L));
    }

    @Test
    void recordDelivery_savesTheResult() {
        AlertContact delivery = AlertContact.created(5L, 1L, "email", "eva@correo.com");
        when(repository.find(40L)).thenReturn(Optional.of(delivery));

        service.recordDelivery(40L, "sent", null);

        assertEquals("sent", delivery.getStatus());
        assertEquals(1, delivery.getAttempts());
        verify(repository).save(delivery);
    }

    @Test
    void alertsNotifiedTo_withoutEmailDoesNotQuery() {
        assertEquals(List.of(), service.alertsNotifiedTo(" "));
        verify(repository, never()).alertsNotifiedTo(any());

        when(repository.alertsNotifiedTo("eva@correo.com")).thenReturn(List.of(5L));
        assertEquals(List.of(5L), service.alertsNotifiedTo(" eva@correo.com "));
    }

    @Test
    void contactsOf_anActiveAlertShowsTheRealStatus() {
        when(contacts.ofUser(10L)).thenReturn(List.of());
        AlertContact delivery = AlertContact.created(5L, 1L, "push", "App");
        delivery.setSentAt(LocalDateTime.now());
        when(repository.ofAlert(5L)).thenReturn(List.of(delivery));

        List<NotifiedContact> result = service.contactsOf(alert("active"));

        assertEquals("Contacto", result.get(0).name());
        assertEquals("pending", result.get(0).channels().get(0).status());
    }
}
