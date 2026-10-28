package com.alertamujer.settings.adapter.out.persistence;

import com.alertamujer.settings.application.port.out.ConfigurationRepository;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Output adapter: the configuration in PostgreSQL (admin.system_configuration). */
@Component
class ConfigurationPersistenceAdapter implements ConfigurationRepository {

    private final SystemConfigurationJpaRepository jpa;

    ConfigurationPersistenceAdapter(SystemConfigurationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<SystemConfiguration> current() {
        return jpa.findBySingletonKey(SystemConfiguration.KEY).map(ConfigurationPersistenceAdapter::toDomain);
    }

    @Override
    public SystemConfiguration save(SystemConfiguration config) {
        return toDomain(jpa.save(toRow(config)));
    }

    static SystemConfiguration toDomain(SystemConfigurationJpaEntity f) {
        SystemConfiguration d = new SystemConfiguration();
        d.setId(f.getId());
        d.setSingletonKey(f.getSingletonKey());
        d.setSosMaxDurationMinutes(f.getSosMaxDurationMinutes());
        d.setEmergencyPhoneNumber(f.getEmergencyPhoneNumber());
        d.setAdminEmail(f.getAdminEmail());
        d.setNotificationsEnabled(f.getNotificationsEnabled());
        d.setDefaultNotificationPriority(f.getDefaultNotificationPriority());
        d.setDefaultTheme(f.getDefaultTheme());
        d.setMaxEmergencyContacts(f.getMaxEmergencyContacts());
        d.setMinAppVersion(f.getMinAppVersion());
        d.setUpdatedByAdminId(f.getUpdatedByAdminId());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static SystemConfigurationJpaEntity toRow(SystemConfiguration d) {
        SystemConfigurationJpaEntity f = new SystemConfigurationJpaEntity();
        f.setId(d.getId());
        f.setSingletonKey(d.getSingletonKey());
        f.setSosMaxDurationMinutes(d.getSosMaxDurationMinutes());
        f.setEmergencyPhoneNumber(d.getEmergencyPhoneNumber());
        f.setAdminEmail(d.getAdminEmail());
        f.setNotificationsEnabled(d.getNotificationsEnabled());
        f.setDefaultNotificationPriority(d.getDefaultNotificationPriority());
        f.setDefaultTheme(d.getDefaultTheme());
        f.setMaxEmergencyContacts(d.getMaxEmergencyContacts());
        f.setMinAppVersion(d.getMinAppVersion());
        f.setUpdatedByAdminId(d.getUpdatedByAdminId());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
