package com.alertamujer.settings.application.service;

import com.alertamujer.settings.application.port.in.ManageSettingsUseCase;
import com.alertamujer.settings.application.port.in.QuerySettingsUseCase;
import com.alertamujer.settings.application.port.out.ConfigurationRepository;
import com.alertamujer.settings.domain.model.SettingsChanges;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Global app parameters. Only knows the configuration port. */
@Service
public class SettingsService implements ManageSettingsUseCase, QuerySettingsUseCase {

    private final ConfigurationRepository repository;

    public SettingsService(ConfigurationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration get() {
        return existing();
    }

    @Override
    @Transactional
    public SystemConfiguration update(SettingsChanges changes, Long adminUserId) {
        SystemConfiguration c = existing();
        LocalDateTime now = LocalDateTime.now();
        c.update(changes, adminUserId, now);
        SystemConfiguration saved = repository.save(c);
        saved.setUpdatedAt(now);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration publicPaths() {
        return existing();
    }

    @Override
    @Transactional(readOnly = true)
    public int maxAlertDurationMinutes() {
        return repository.current().map(SystemConfiguration::getSosMaxDurationMinutes).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public String emergencyNumber() {
        return repository.current().map(SystemConfiguration::getEmergencyPhoneNumber).orElse("123");
    }

    @Override
    @Transactional(readOnly = true)
    public int maxContacts() {
        return repository.current().map(SystemConfiguration::getMaxEmergencyContacts).orElse(5);
    }

    private SystemConfiguration existing() {
        return repository.current().orElseThrow(() -> new IllegalStateException("Falta la configuración del sistema"));
    }
}
