package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.SystemConfiguration;
import com.alertamujer.admin.infrastructure.repository.SystemConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Parámetros globales de la app (fila única de admin.system_configuration). */
@Service
public class AdminSettingsService {

    static final int DURACION_MINIMA = 30;
    static final int DURACION_MAXIMA = 1440;

    private final SystemConfigurationRepository repository;

    public AdminSettingsService(SystemConfigurationRepository repository) {
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
        return new PublicSettings(c.getEmergencyPhoneNumber(), c.getAdminEmail(), c.getSosMaxDurationMinutes());
    }

    @Transactional(readOnly = true)
    public int duracionMaximaAlertaMinutos() {
        return repository.findBySingletonKey("SYSTEM").map(SystemConfiguration::getSosMaxDurationMinutes).orElse(0);
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
        c.setUpdatedByAdminId(adminUserId == null ? null : adminUserId.intValue());
        SystemConfiguration guardada = repository.save(c);
        // updated_at lo pone un trigger de la base; la entidad no lo ve hasta recargarla
        return new SettingsView(guardada.getSosMaxDurationMinutes(), guardada.getEmergencyPhoneNumber(),
                guardada.getAdminEmail(), LocalDateTime.now());
    }

    private SystemConfiguration buscar() {
        return repository.findBySingletonKey("SYSTEM")
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del sistema"));
    }

    private static SettingsView vista(SystemConfiguration c) {
        return new SettingsView(c.getSosMaxDurationMinutes(), c.getEmergencyPhoneNumber(), c.getAdminEmail(),
                c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt());
    }

    public record SettingsRequest(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail) {}

    public record SettingsView(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                               LocalDateTime updatedAt) {}

    public record PublicSettings(String emergencyPhoneNumber, String supportEmail, Integer sosMaxDurationMinutes) {}
}
