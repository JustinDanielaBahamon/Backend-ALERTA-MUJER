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
        when(settingsService.maximoContactos()).thenReturn(2);
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
}
