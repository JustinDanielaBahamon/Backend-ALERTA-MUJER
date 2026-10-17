package com.alertamujer.alerts.application.service;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.identity.api.IdentityApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private IdentityApi identityApi;

    @Mock
    private SettingsApi settingsService;

    @InjectMocks
    private EmergencyContactService service;

    @BeforeEach
    void setUp() {
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(10L));
        org.mockito.Mockito.lenient().when(settingsService.maximoContactos()).thenReturn(2);
    }

    @Test
    void createContact_permiteHastaElLimite() {
        when(emergencyContactRepository.findByUserProfileId(10L)).thenReturn(List.of(new EmergencyContact()));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContact creado = service.createContact(new EmergencyContact(), 1L);

        assertEquals(10L, creado.getUserProfileId());
    }

    @Test
    void createContact_rechazaAlSuperarElLimite() {
        when(emergencyContactRepository.findByUserProfileId(10L))
                .thenReturn(List.of(new EmergencyContact(), new EmergencyContact()));

        assertThrows(IllegalArgumentException.class, () -> service.createContact(new EmergencyContact(), 1L));
        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void updateContact_soloCambiaLosCamposEnviados() {
        EmergencyContact existente = new EmergencyContact();
        existente.setId(3L);
        existente.setUserProfileId(10L);
        existente.setContactName("Mamá");
        existente.setTelephone("3001112233");
        existente.setEmail("mama@correo.com");
        when(emergencyContactRepository.findById(3L)).thenReturn(java.util.Optional.of(existente));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));
        EmergencyContact cambios = new EmergencyContact();
        cambios.setContactName("Mamá Rosa");

        EmergencyContact actualizado = service.updateContact(3L, cambios, 1L);

        assertEquals("Mamá Rosa", actualizado.getContactName());
        assertEquals("3001112233", actualizado.getTelephone());
        assertEquals("mama@correo.com", actualizado.getEmail());
    }

    @Test
    void updateContact_guardaYQuitaLaFoto() {
        EmergencyContact existente = new EmergencyContact();
        existente.setId(3L);
        existente.setUserProfileId(10L);
        when(emergencyContactRepository.findById(3L)).thenReturn(java.util.Optional.of(existente));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));
        EmergencyContact conFoto = new EmergencyContact();
        conFoto.setPhoto("data:image/jpeg;base64,/9j/4AAQ");

        assertEquals("data:image/jpeg;base64,/9j/4AAQ", service.updateContact(3L, conFoto, 1L).getPhoto());
        assertEquals("data:image/jpeg;base64,/9j/4AAQ", service.updateContact(3L, new EmergencyContact(), 1L).getPhoto());

        EmergencyContact sinFoto = new EmergencyContact();
        sinFoto.setPhoto("");
        assertNull(service.updateContact(3L, sinFoto, 1L).getPhoto());
    }

    @Test
    void createContact_rechazaUnaFotoQueNoEsImagen() {
        when(emergencyContactRepository.findByUserProfileId(10L)).thenReturn(List.of());
        EmergencyContact contacto = new EmergencyContact();
        contacto.setPhoto("<script>");

        assertThrows(IllegalArgumentException.class, () -> service.createContact(contacto, 1L));
        verify(emergencyContactRepository, never()).save(any());
    }
}
