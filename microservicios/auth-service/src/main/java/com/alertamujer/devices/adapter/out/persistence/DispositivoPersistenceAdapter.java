package com.alertamujer.devices.adapter.out.persistence;

import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los dispositivos en PostgreSQL. */
@Component
class DispositivoPersistenceAdapter implements DispositivoRepositorio {

    private final DeviceJpaRepository jpa;

    DispositivoPersistenceAdapter(DeviceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Device save(Device dispositivo) {
        return aDominio(jpa.save(aFila(dispositivo)));
    }

    @Override
    public Optional<Device> find(Long id) {
        return jpa.findById(id).map(DispositivoPersistenceAdapter::aDominio);
    }

    @Override
    public List<Device> todos() {
        return jpa.findAll().stream().map(DispositivoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Device> deCuenta(Long accountId) {
        return jpa.findByAccountId(accountId).stream().map(DispositivoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public long contar() {
        return jpa.count();
    }

    static Device aDominio(DeviceJpaEntity f) {
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

    static DeviceJpaEntity aFila(Device d) {
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
