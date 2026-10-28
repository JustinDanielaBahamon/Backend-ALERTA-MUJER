package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.QueryDevicesUseCase;
import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Push tokens and primary phone for the other modules and Alert Service. */
@Service
@Transactional(readOnly = true)
public class DeviceQueryService implements QueryDevicesUseCase {

    private final DeviceRepository repository;
    private final IdentityPort identity;

    public DeviceQueryService(DeviceRepository repository, IdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Override
    public List<String> profileTokens(Long profile) {
        // Devices are stored per account: profile -> user -> account
        return identity.userOfProfile(profile).flatMap(identity::accountOf).map(this::tokens).orElse(List.of());
    }

    @Override
    public List<String> userTokens(Long userId) {
        return identity.accountOf(userId).map(this::tokens).orElse(List.of());
    }

    @Override
    public List<String> phoneTokens(Long userId) {
        return identity.accountOf(userId).map(account -> repository.ofAccount(account).stream()
                        .filter(Device::isPhone)
                        .filter(Device::hasPushToken)
                        .map(Device::getFcmToken)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public boolean isTheNewestPhone(Long userId, Long deviceId) {
        if (deviceId == null) return true;
        return identity.accountOf(userId)
                .map(account -> Device.isTheNewestPhone(repository.ofAccount(account), deviceId))
                .orElse(true);
    }

    @Override
    public long count() {
        return repository.count();
    }

    private List<String> tokens(Long account) {
        return repository.ofAccount(account).stream().filter(Device::hasPushToken).map(Device::getFcmToken).toList();
    }
}
