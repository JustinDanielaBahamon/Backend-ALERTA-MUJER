package com.alertamujer.devices.application.service;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final IdentityApi identityApi;
    private final ApplicationEventPublisher events;

    public DeviceService(DeviceRepository deviceRepository, IdentityApi identityApi, ApplicationEventPublisher events) {
        this.deviceRepository = deviceRepository;
        this.identityApi = identityApi;
        this.events = events;
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
        device.setLastAccess(LocalDateTime.now());
        Device guardado = deviceRepository.save(device);
        avisarNuevoCelular(guardado);
        return guardado;
    }

    /**
     * Se entró a la cuenta desde otro celular: se avisa a los demás celulares de la usuaria por si
     * no fue ella (lo envía el módulo de notificaciones).
     */
    private void avisarNuevoCelular(Device nuevo) {
        if (DevicesApiService.OS_WEB.equalsIgnoreCase(nuevo.getOsName())) return;
        List<String> tokens = deviceRepository.findByAccountId(nuevo.getAccountId()).stream()
                .filter(d -> !d.getId().equals(nuevo.getId()))
                .filter(d -> !DevicesApiService.OS_WEB.equalsIgnoreCase(d.getOsName()))
                .map(Device::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();
        if (tokens.isEmpty()) return;
        events.publishEvent(new NewPhoneSignedInEvent(nombreCelular(nuevo), tokens));
    }

    static String nombreCelular(Device d) {
        if (d.getDeviceName() != null && !d.getDeviceName().isBlank()) return d.getDeviceName();
        String marcaModelo = ((d.getBrand() == null ? "" : d.getBrand()) + " " + (d.getModel() == null ? "" : d.getModel())).trim();
        return marcaModelo.isEmpty() ? "otro celular" : marcaModelo;
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

    /**
     * La app lo llama al abrirse: actualiza los datos del celular que vengan (marca, modelo, nombre,
     * versiones, GPS) sin borrar los que no envía, y marca la última conexión.
     */
    @Transactional
    public Device registrarUso(Long id, Device datos, Long currentUserId) {
        Device existing = getDeviceById(id, currentUserId);
        if (datos.getBrand() != null) existing.setBrand(recortar(datos.getBrand(), 50));
        if (datos.getModel() != null) existing.setModel(recortar(datos.getModel(), 100));
        if (datos.getDeviceName() != null) existing.setDeviceName(recortar(datos.getDeviceName(), 100));
        if (datos.getOsName() != null) existing.setOsName(recortar(datos.getOsName(), 30));
        if (datos.getOsVersion() != null) existing.setOsVersion(recortar(datos.getOsVersion(), 30));
        if (datos.getAppVersion() != null) existing.setAppVersion(recortar(datos.getAppVersion(), 30));
        if (datos.getGpsStatus() != null) existing.setGpsStatus(datos.getGpsStatus());
        if (datos.getPermissions() != null) {
            existing.setPermissions(validarPermisos(datos.getPermissions()));
            existing.setPermissionsUpdatedAt(LocalDateTime.now());
        }
        existing.setLastAccess(LocalDateTime.now());
        return deviceRepository.save(existing);
    }

    // Solo un objeto plano de nombres cortos con true/false: lo muestra la web, no se guarda otra cosa
    private static final java.util.regex.Pattern PERMISOS =
            java.util.regex.Pattern.compile("^\\{(\"[a-zA-Z]{1,30}\":(true|false)(,\"[a-zA-Z]{1,30}\":(true|false))*)?}$");

    static String validarPermisos(String permisos) {
        String valor = permisos.replaceAll("\\s", "");
        if (valor.length() > 400 || !PERMISOS.matcher(valor).matches()) {
            throw new IllegalArgumentException("Permisos del celular no válidos");
        }
        return valor;
    }

    private static String recortar(String valor, int max) {
        String v = valor.trim();
        return v.length() > max ? v.substring(0, max) : v;
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
}