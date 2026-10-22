package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.ManageDevicesUseCase;
import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Dispositivos para el panel: datos del celular más la dueña (el dispositivo solo guarda la cuenta). */
@Service
public class DeviceAdministrationService implements ManageDevicesUseCase {

    private final DeviceRepository repository;
    private final IdentityPort identity;

    public DeviceAdministrationService(DeviceRepository repository, IdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceWithOwner> listAll() {
        Map<Long, UserSummary> owners = identity.ownersByAccount();
        return repository.all().stream()
                .sorted(Comparator.comparing(Device::getLastAccess, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(d -> new DeviceWithOwner(d, owners.get(d.getAccountId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceWithOwner get(Long id) {
        Device d = existing(id);
        return new DeviceWithOwner(d, identity.accountOwner(d.getAccountId()).orElse(null));
    }

    @Override
    @Transactional
    public DeviceWithOwner changeStatus(Long id, String status) {
        if (status == null || !Device.STATUSES.contains(status)) throw new IllegalArgumentException("Estado no válido: " + status);
        Device d = existing(id);
        d.changeStatus(status);
        repository.save(d);
        return get(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(existing(id).getId());
    }

    private Device existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }
}
