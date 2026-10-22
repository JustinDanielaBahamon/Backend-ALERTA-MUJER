package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.MyDevicesUseCase;
import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.DeviceEventsPort;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.DeviceData;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Dispositivos de la persona en sesión. Solo conoce puertos. */
@Service
public class MyDevicesService implements MyDevicesUseCase {

    private final DeviceRepository repository;
    private final IdentityPort identity;
    private final DeviceEventsPort events;

    public MyDevicesService(DeviceRepository repository, IdentityPort identity, DeviceEventsPort events) {
        this.repository = repository;
        this.identity = identity;
        this.events = events;
    }

    @Override
    public List<Device> mine(Long userId) {
        return repository.ofAccount(identity.requiredAccount(userId));
    }

    @Override
    public Device one(Long id, Long userId) {
        Device d = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
        if (!d.esDe(identity.requiredAccount(userId))) {
            throw new AccessDeniedException("No puedes acceder a dispositivos de otro usuario");
        }
        return d;
    }

    @Override
    @Transactional
    public Device registrar(DeviceData data, Long userId) {
        Long account = identity.requiredAccount(userId);
        LocalDateTime now = LocalDateTime.now();
        // El mismo celular volviendo a registrarse (app reinstalada o datos borrados): se reutiliza el
        // registro en vez de duplicarlo, y no se avisa de "otro celular"
        Optional<Device> same = repository.ofAccount(account).stream()
                .filter(d -> d.isSamePhone(data))
                .max(Comparator.comparing(Device::getLastAccess, Comparator.nullsFirst(Comparator.naturalOrder())));
        if (same.isPresent()) {
            Device d = same.get();
            d.registrarUso(data, now);
            return repository.save(d);
        }
        Device stored = repository.save(Device.created(account, data, now));
        notifyNewPhone(stored);
        return stored;
    }

    @Override
    @Transactional
    public Device replace(Long id, DeviceData data, Long userId) {
        Device d = one(id, userId);
        d.replace(data);
        return repository.save(d);
    }

    @Override
    @Transactional
    public Device registrarUso(Long id, DeviceData data, Long userId) {
        Device d = one(id, userId);
        d.registrarUso(data, LocalDateTime.now());
        return repository.save(d);
    }

    @Override
    @Transactional
    public void changePushToken(Long id, String token, Long userId) {
        Device d = one(id, userId);
        d.changePushToken(token);
        repository.save(d);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(one(id, userId).getId());
    }

    /** Se entró desde otro celular: se avisa a los demás celulares de la usuaria por si no fue ella. */
    private void notifyNewPhone(Device created) {
        if (!created.isPhone()) return;
        List<String> tokens = repository.ofAccount(created.getAccountId()).stream()
                .filter(d -> !d.getId().equals(created.getId()))
                .filter(Device::isPhone)
                .filter(Device::hasPushToken)
                .map(Device::getFcmToken)
                .toList();
        if (!tokens.isEmpty()) events.newPhone(created.nameForNotice(), tokens);
    }
}
