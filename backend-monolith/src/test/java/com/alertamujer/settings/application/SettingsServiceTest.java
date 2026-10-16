package com.alertamujer.settings.application;

import com.alertamujer.settings.domain.model.SystemConfiguration;
import com.alertamujer.settings.infrastructure.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private SystemConfigurationRepository repository;

    @InjectMocks
    private SettingsService service;

    private final SystemConfiguration config = new SystemConfiguration();

    @BeforeEach
    void setUp() {
        config.setSosMaxDurationMinutes(30);
        config.setEmergencyPhoneNumber("123");
        config.setAdminEmail("soporte@alerta.test");
        when(repository.findBySingletonKey("SYSTEM")).thenReturn(Optional.of(config));
    }

    @Test
    void actualizar_cambiaSoloLoEnviado() {
        when(repository.save(any(SystemConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        SettingsService.SettingsView vista = service.actualizar(
                new SettingsService.SettingsRequest(120, null, " ayuda@alerta.test ", 8, "1.2.0"), 9L);

        assertEquals(120, vista.sosMaxDurationMinutes());
        assertEquals("123", vista.emergencyPhoneNumber());
        assertEquals("ayuda@alerta.test", vista.supportEmail());
        assertEquals(8, vista.maxEmergencyContacts());
        assertEquals("1.2.0", vista.minAppVersion());
        assertEquals(9, config.getUpdatedByAdminId());

        // "" quita la versión mínima
        service.actualizar(new SettingsService.SettingsRequest(null, null, null, null, ""), 9L);
        assertEquals(null, config.getMinAppVersion());
    }

    @Test
    void actualizar_rechazaValoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new SettingsService.SettingsRequest(5, null, null, null, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new SettingsService.SettingsRequest(null, "12a", null, null, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new SettingsService.SettingsRequest(null, null, "sin-arroba", null, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new SettingsService.SettingsRequest(null, null, null, 0, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new SettingsService.SettingsRequest(null, null, null, null, "v1.2"), 9L));
    }
}
