package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.ProtectionDevice;

import java.util.Optional;

/** Output port: phones with automatic protection turned on and the hash of their key. */
public interface ProtectionDeviceRepository {

    ProtectionDevice save(ProtectionDevice device);

    Optional<ProtectionDevice> byKeyHash(String keyHash);

    /** Turning it on again in the same phone replaces its previous key. */
    void deleteOf(Long profile, Long deviceId);

    boolean exists(Long profile, Long deviceId);
}
