package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.ProtectionDeviceRepository;
import com.alertamujer.alerts.domain.model.ProtectionDevice;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Output adapter: phones with automatic protection in PostgreSQL. */
@Component
class ProtectionDevicePersistenceAdapter implements ProtectionDeviceRepository {

    private final ProtectionDeviceJpaRepository jpa;

    ProtectionDevicePersistenceAdapter(ProtectionDeviceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ProtectionDevice save(ProtectionDevice device) {
        ProtectionDeviceJpaEntity row = new ProtectionDeviceJpaEntity();
        row.setId(device.getId());
        row.setUserProfileId(device.getUserProfileId());
        row.setUserId(device.getUserId());
        row.setDeviceId(device.getDeviceId());
        row.setKeyHash(device.getKeyHash());
        row.setCreatedAt(device.getCreatedAt());
        return toDomain(jpa.save(row));
    }

    @Override
    public Optional<ProtectionDevice> byKeyHash(String keyHash) {
        return jpa.findByKeyHash(keyHash).map(ProtectionDevicePersistenceAdapter::toDomain);
    }

    @Override
    public void deleteOf(Long profile, Long deviceId) {
        jpa.deleteByUserProfileIdAndDeviceId(profile, deviceId);
    }

    @Override
    public boolean exists(Long profile, Long deviceId) {
        return jpa.existsByUserProfileIdAndDeviceId(profile, deviceId);
    }

    private static ProtectionDevice toDomain(ProtectionDeviceJpaEntity row) {
        ProtectionDevice d = new ProtectionDevice();
        d.setId(row.getId());
        d.setUserProfileId(row.getUserProfileId());
        d.setUserId(row.getUserId());
        d.setDeviceId(row.getDeviceId());
        d.setKeyHash(row.getKeyHash());
        d.setCreatedAt(row.getCreatedAt());
        return d;
    }
}
