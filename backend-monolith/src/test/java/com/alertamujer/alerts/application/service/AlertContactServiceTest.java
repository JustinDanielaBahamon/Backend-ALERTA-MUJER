package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertContact;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.AlertContactRepository;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertContactServiceTest {

    @Mock
    private AlertContactRepository alertContactRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private IdentityApi identityApi;

    @Mock
    private org.springframework.context.ApplicationEventPublisher events;

    @InjectMocks
    private AlertContactService service;

    private Alert alerta(String status) {
        Alert alert = new Alert();
        alert.setId(5L);
        alert.setUserProfileId(10L);
        alert.setStatus(status);
        return alert;
    }

    private EmergencyContact contacto(Long id, String nombre, String correo) {
        EmergencyContact c = new EmergencyContact();
        c.setId(id);
        c.setContactName(nombre);
        c.setEmail(correo);
        return c;
    }

    @Test
    @SuppressWarnings("unchecked")
    void registrarDestinatarios_creaCorreoYPushSegunElContacto() {
        when(emergencyContactRepository.findByUserProfileId(10L)).thenReturn(List.of(
                contacto(1L, "Con app", "app@correo.com"),
                contacto(2L, "Solo correo", "otra@correo.com"),
                contacto(3L, "Sin correo", null)));
        when(identityApi.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(identityApi.findUserByEmail("app@correo.com")).thenReturn(Optional.of(new UserSummary(3L, "Eva", null, "app@correo.com", null, 1L, null)));

        Alert activa = alerta("active");
        service.registrarDestinatarios(activa);

        ArgumentCaptor<List<AlertContact>> captor = ArgumentCaptor.forClass(List.class);
        verify(alertContactRepository).saveAll(captor.capture());
        List<AlertContact> registros = captor.getValue();
        assertEquals(3, registros.size());
        assertEquals("email", registros.get(0).getChannel());
        assertEquals("push", registros.get(1).getChannel());
        assertEquals(1L, registros.get(1).getEmergencyContactId());
        assertEquals(2L, registros.get(2).getEmergencyContactId());
        assertEquals("pending", registros.get(2).getStatus());
        verify(events).publishEvent(new com.alertamujer.alerts.api.AlertActivatedEvent(5L, 10L, activa.getStartedAt()));
    }

    @Test
    void contactosDeAlerta_envioAbiertoDeAlertaTerminadaQuedaExpirado() {
        Alert alert = alerta("cancelled");
        alert.setCancelledAt(LocalDateTime.now());
        AlertContact pendiente = new AlertContact();
        pendiente.setEmergencyContactId(1L);
        pendiente.setChannel("email");
        pendiente.setDestination("app@correo.com");
        AlertContact entregado = new AlertContact();
        entregado.setEmergencyContactId(1L);
        entregado.setChannel("push");
        entregado.setStatus("delivered");

        when(emergencyContactRepository.findByUserProfileId(10L)).thenReturn(List.of(contacto(1L, "Mamá", "app@correo.com")));
        when(alertContactRepository.findByAlertId(5L)).thenReturn(List.of(pendiente, entregado));

        List<AlertContactService.ContactoNotificado> resultado = service.contactosDeAlerta(alert);

        assertEquals(1, resultado.size());
        assertEquals("Mamá", resultado.get(0).name());
        assertEquals("expired", resultado.get(0).channels().get(0).status());
        assertEquals("delivered", resultado.get(0).channels().get(1).status());
    }
}
