package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device device) {
        Device existing = getDeviceById(id);
        existing.setDeviceUuid(device.getDeviceUuid());
        existing.setBrand(device.getBrand());
        existing.setModel(device.getModel());
        existing.setOsName(device.getOsName());
        existing.setOsVersion(device.getOsVersion());
        existing.setAppVersion(device.getAppVersion());
        existing.setGpsStatus(device.getGpsStatus());
        existing.setStatus(device.getStatus());
        existing.setLastAccess(device.getLastAccess());
        return deviceRepository.save(existing);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
}
