package com.alertamujer.settings.application.service;

import com.alertamujer.settings.application.port.out.ConfigurationRepository;
import com.alertamujer.settings.domain.model.SettingsChanges;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SettingsServiceTest {

    private final ConfigurationRepository repository = mock(ConfigurationRepository.class);
    private final SettingsService service = new SettingsService(repository);

    @Test
    void withoutConfigurationUsesSafeValuesForOtherModules() {
        when(repository.current()).thenReturn(Optional.empty());

        assertEquals(0, service.maxAlertDurationMinutes());
        assertEquals("123", service.emergencyNumber());
        assertEquals(5, service.maxContacts());
        assertThrows(IllegalStateException.class, service::get);
    }

    @Test
    void updateSavesAndReturnsTheChangeTime() {
        SystemConfiguration c = new SystemConfiguration();
        c.setSosMaxDurationMinutes(120);
        when(repository.current()).thenReturn(Optional.of(c));
        when(repository.save(any())).thenAnswer(inv -> {
            SystemConfiguration saved = inv.getArgument(0);
            saved.setUpdatedAt(null); // la base no devuelve la hora del trigger
            return saved;
        });

        SystemConfiguration r = service.update(new SettingsChanges(90, null, null, null, null), 7L);

        assertEquals(90, r.getSosMaxDurationMinutes());
        assertNotNull(r.getUpdatedAt());
        verify(repository).save(c);
    }
}
