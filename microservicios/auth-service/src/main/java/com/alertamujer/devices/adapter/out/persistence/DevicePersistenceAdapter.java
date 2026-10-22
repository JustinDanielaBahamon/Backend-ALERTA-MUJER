package com.alertamujer.devices.adapter.out.persistence;

import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los dispositivos en PostgreSQL. */
@Component
class DevicePersistenceAdapter implements DeviceRepository {

    private final DeviceJpaRepository jpa;

    DevicePersistenceAdapter(DeviceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Device save(Device device) {
        return toDomain(jpa.save(toRow(device)));
    }

    @Override
    public Optional<Device> find(Long id) {
        return jpa.findById(id).map(DevicePersistenceAdapter::toDomain);
    }

    @Override
    public List<Device> all() {
        return jpa.findAll().stream().map(DevicePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Device> ofAccount(Long accountId) {
        return jpa.findByAccountId(accountId).stream().map(DevicePersistenceAdapter::toDomain).toList();
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    static Device toDomain(DeviceJpaEntity f) {
        Device d = new Device();
        d.setId(f.getId());
        d.setAccountId(f.getAccountId());
        d.setDeviceUuid(f.getDeviceUuid());
        d.setBrand(f.getBrand());
        d.setModel(f.getModel());
        d.setDeviceName(f.getDeviceName());
        d.setOsName(f.getOsName());
        d.setOsVersion(f.getOsVersion());
        d.setAppVersion(f.getAppVersion());
        d.setGpsStatus(f.getGpsStatus());
        d.setStatus(f.getStatus());
        d.setLastAccess(f.getLastAccess());
        d.setCreatedAt(f.getCreatedAt());
        d.setFcmToken(f.getFcmToken());
        d.setPermissions(f.getPermissions());
        d.setPermissionsUpdatedAt(f.getPermissionsUpdatedAt());
        return d;
    }

    static DeviceJpaEntity toRow(Device d) {
        DeviceJpaEntity f = new DeviceJpaEntity();
        f.setId(d.getId());
        f.setAccountId(d.getAccountId());
        f.setDeviceUuid(d.getDeviceUuid());
        f.setBrand(d.getBrand());
        f.setModel(d.getModel());
        f.setDeviceName(d.getDeviceName());
        f.setOsName(d.getOsName());
        f.setOsVersion(d.getOsVersion());
        f.setAppVersion(d.getAppVersion());
        f.setGpsStatus(d.getGpsStatus());
        f.setStatus(d.getStatus());
        f.setLastAccess(d.getLastAccess());
        f.setCreatedAt(d.getCreatedAt());
        f.setFcmToken(d.getFcmToken());
        f.setPermissions(d.getPermissions());
        f.setPermissionsUpdatedAt(d.getPermissionsUpdatedAt());
        return f;
    }
}
