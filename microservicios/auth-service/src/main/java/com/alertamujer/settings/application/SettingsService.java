package com.alertamujer.settings.application;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import com.alertamujer.settings.infrastructure.repository.SystemConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Módulo de configuración del sistema: parámetros globales de la app (fila única de
 * admin.system_configuration). Lo editan las administradoras y lo leen los demás módulos por SettingsApi.
 */
@Service
public class SettingsService implements SettingsApi {

    static final int DURACION_MINIMA = 30;
    static final int DURACION_MAXIMA = 1440;

    private final SystemConfigurationRepository repository;

    public SettingsService(SystemConfigurationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SettingsView obtener() {
        return vista(buscar());
    }

    /** Lo que necesitan las apps de cualquier usuaria: número de emergencia, soporte y duración de una alerta. */
    @Transactional(readOnly = true)
    public PublicSettings publicos() {
        SystemConfiguration c = buscar();
        return new PublicSettings(c.getEmergencyPhoneNumber(), c.getAdminEmail(), c.getSosMaxDurationMinutes(),
                c.getMaxEmergencyContacts(), c.getMinAppVersion());
    }

    @Override
    @Transactional(readOnly = true)
    public int duracionMaximaAlertaMinutos() {
        return repository.findBySingletonKey("SYSTEM").map(SystemConfiguration::getSosMaxDurationMinutes).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public String numeroEmergencia() {
        return repository.findBySingletonKey("SYSTEM").map(SystemConfiguration::getEmergencyPhoneNumber).orElse("123");
    }

    /** Máximo de contactos de emergencia por usuaria (5 si no hay configuración). */
    @Override
    @Transactional(readOnly = true)
    public int maximoContactos() {
        return repository.findBySingletonKey("SYSTEM").map(SystemConfiguration::getMaxEmergencyContacts).orElse(5);
    }

    @Transactional
    public SettingsView actualizar(SettingsRequest req, Long adminUserId) {
        SystemConfiguration c = buscar();
        if (req.sosMaxDurationMinutes() != null) {
            int minutos = req.sosMaxDurationMinutes();
            if (minutos < DURACION_MINIMA || minutos > DURACION_MAXIMA) {
                throw new IllegalArgumentException("La duración de una alerta debe estar entre "
                        + DURACION_MINIMA + " y " + DURACION_MAXIMA + " minutos");
            }
            c.setSosMaxDurationMinutes(minutos);
        }
        if (req.emergencyPhoneNumber() != null) {
            String numero = req.emergencyPhoneNumber().trim();
            if (!numero.matches("\\+?\\d{3,15}")) throw new IllegalArgumentException("Número de emergencia no válido");
            c.setEmergencyPhoneNumber(numero);
        }
        if (req.supportEmail() != null) {
            String correo = req.supportEmail().trim();
            if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("Correo de soporte no válido");
            c.setAdminEmail(correo);
        }
        if (req.maxEmergencyContacts() != null) {
            int maximo = req.maxEmergencyContacts();
            if (maximo < 1 || maximo > 20) throw new IllegalArgumentException("El límite de contactos debe estar entre 1 y 20");
            c.setMaxEmergencyContacts(maximo);
        }
        if (req.minAppVersion() != null) {
            String version = req.minAppVersion().trim();
            if (!version.isEmpty() && !version.matches("\\d+(\\.\\d+){0,2}")) {
                throw new IllegalArgumentException("Versión no válida: usa el formato 1.2.0");
            }
            c.setMinAppVersion(version.isEmpty() ? null : version);
        }
        c.setUpdatedByAdminId(adminUserId == null ? null : adminUserId.intValue());
        SystemConfiguration guardada = repository.save(c);
        // updated_at lo pone un trigger de la base; la entidad no lo ve hasta recargarla
        return vista(guardada, LocalDateTime.now());
    }

    private SystemConfiguration buscar() {
        return repository.findBySingletonKey("SYSTEM")
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del sistema"));
    }

    private static SettingsView vista(SystemConfiguration c) {
        return vista(c, c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt());
    }

    private static SettingsView vista(SystemConfiguration c, LocalDateTime actualizado) {
        return new SettingsView(c.getSosMaxDurationMinutes(), c.getEmergencyPhoneNumber(), c.getAdminEmail(),
                c.getMaxEmergencyContacts(), c.getMinAppVersion(), actualizado);
    }

    /** minAppVersion vacío ("") quita la versión mínima. */
    public record SettingsRequest(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                                  Integer maxEmergencyContacts, String minAppVersion) {}

    public record SettingsView(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                               Integer maxEmergencyContacts, String minAppVersion, LocalDateTime updatedAt) {}

    public record PublicSettings(String emergencyPhoneNumber, String supportEmail, Integer sosMaxDurationMinutes,
                                 Integer maxEmergencyContacts, String minAppVersion) {}
}
