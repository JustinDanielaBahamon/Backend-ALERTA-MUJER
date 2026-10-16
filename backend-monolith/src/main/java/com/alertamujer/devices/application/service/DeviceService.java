package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final IdentityApi identityApi;

    public DeviceService(DeviceRepository deviceRepository, IdentityApi identityApi) {
        this.deviceRepository = deviceRepository;
        this.identityApi = identityApi;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getDevicesByCurrentUser(Long currentUserId) {
        Long accountId = identityApi.requireAccountId(currentUserId);
        return deviceRepository.findByAccountId(accountId);
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

    @Transactional
    public Device createDevice(Device device, Long currentUserId) {
        Long accountId = identityApi.requireAccountId(currentUserId);
        device.setAccountId(accountId);
        return deviceRepository.save(device);
    }

    @Transactional
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
        // Actualizar token FCM si viene proporcionado
        if (device.getFcmToken() != null && !device.getFcmToken().isEmpty()) {
            existing.setFcmToken(device.getFcmToken());
        }
        return deviceRepository.save(existing);
    }

    @Transactional
    public void deleteDevice(Long id, Long currentUserId) {
        Device device = getDeviceById(id, currentUserId);
        deviceRepository.deleteById(id);
    }

    private void validateOwnership(Device device, Long currentUserId) {
        Long accountId = identityApi.requireAccountId(currentUserId);
        if (!device.getAccountId().equals(accountId)) {
            throw new AccessDeniedException("No puedes acceder a dispositivos de otro usuario");
        }
    }

    /**
     * Actualizar solo el token FCM de un dispositivo
     */
    @Transactional
    public Device updateFcmToken(Long id, String fcmToken, Long currentUserId) {
        Device device = getDeviceById(id, currentUserId);
        device.setFcmToken(fcmToken);
        return deviceRepository.save(device);
    }

    /**
     * Obtener todos los tokens FCM activos de los dispositivos de un usuario
     */
    public List<String> getFcmTokensByUser(Long userProfileId) {
        List<Device> devices = deviceRepository.findByAccountId(userProfileId);
        return devices.stream()
                .map(Device::getFcmToken)
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());
    }
}