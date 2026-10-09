package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final AccountRepository accountRepository;

    public DeviceService(DeviceRepository deviceRepository, AccountRepository accountRepository) {
        this.deviceRepository = deviceRepository;
        this.accountRepository = accountRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getDevicesByUser(Long currentUserId) {
        Account account = accountRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Account for user", currentUserId));
        return deviceRepository.findByAccountId(account.getId());
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    public Device getDeviceById(Long id, Long currentUserId) {
        Device device = getDeviceById(id);
        validateOwnership(device, currentUserId);
        return device;
    }

    public Device createDevice(Device device, Long currentUserId) {
        Account account = accountRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Account for user", currentUserId));
        device.setAccountId(account.getId());
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device device, Long currentUserId) {
        Device existing = getDeviceById(id, currentUserId);
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

    public void deleteDevice(Long id, Long currentUserId) {
        Device device = getDeviceById(id, currentUserId);
        deviceRepository.deleteById(id);
    }

    private void validateOwnership(Device device, Long currentUserId) {
        Account account = accountRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Account for user", currentUserId));
        if (!device.getAccountId().equals(account.getId())) {
            throw new AccessDeniedException("No puedes acceder a dispositivos de otro usuario");
        }
    }
}
