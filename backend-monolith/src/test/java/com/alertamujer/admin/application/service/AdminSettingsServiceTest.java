package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.SystemConfiguration;
import com.alertamujer.admin.infrastructure.repository.SystemConfigurationRepository;
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
class AdminSettingsServiceTest {

    @Mock
    private SystemConfigurationRepository repository;

    @InjectMocks
    private AdminSettingsService service;

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

        AdminSettingsService.SettingsView vista = service.actualizar(
                new AdminSettingsService.SettingsRequest(120, null, " ayuda@alerta.test "), 9L);

        assertEquals(120, vista.sosMaxDurationMinutes());
        assertEquals("123", vista.emergencyPhoneNumber());
        assertEquals("ayuda@alerta.test", vista.supportEmail());
        assertEquals(9, config.getUpdatedByAdminId());
    }

    @Test
    void actualizar_rechazaValoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new AdminSettingsService.SettingsRequest(5, null, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new AdminSettingsService.SettingsRequest(null, "12a", null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(
                new AdminSettingsService.SettingsRequest(null, null, "sin-arroba"), 9L));
    }
}
