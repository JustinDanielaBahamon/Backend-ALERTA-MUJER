package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dispositivos para el panel de administración: datos del celular más la usuaria dueña
 * (la entidad Device solo guarda accountId). La administradora puede ver, cambiar el estado
 * y borrar cualquier dispositivo; los demás endpoints solo dejan hacerlo a la dueña.
 */
@Service
public class AdminDeviceService {

    private static final Set<String> ESTADOS = Set.of("active", "inactive", "blocked", "lost");

    private final DeviceRepository deviceRepository;
    private final IdentityApi identityApi;

    public AdminDeviceService(DeviceRepository deviceRepository, IdentityApi identityApi) {
        this.deviceRepository = deviceRepository;
        this.identityApi = identityApi;
    }

    @Transactional(readOnly = true)
    public List<AdminDeviceView> listar() {
        Map<Long, UserSummary> duenaPorCuenta = identityApi.usersByAccountId();
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(Device::getLastAccess, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(d -> vista(d, duenaPorCuenta.get(d.getAccountId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDeviceView obtener(Long id) {
        Device d = buscar(id);
        UserSummary dueña = identityApi.findUserByAccountId(d.getAccountId()).orElse(null);
        return vista(d, dueña);
    }

    @Transactional
    public AdminDeviceView cambiarEstado(Long id, String estado) {
        if (estado == null || !ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }
        Device d = buscar(id);
        d.setStatus(estado);
        deviceRepository.save(d);
        return obtener(id);
    }

    @Transactional
    public void eliminar(Long id) {
        deviceRepository.delete(buscar(id));
    }

    private Device buscar(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    private AdminDeviceView vista(Device d, UserSummary u) {
        String nombre = u == null ? null : u.fullName();
        return new AdminDeviceView(d.getId(), d.getAccountId(), u != null ? u.id() : null,
                nombre, u != null ? u.email() : null, u != null ? u.telephone() : null,
                d.getDeviceUuid(), d.getBrand(), d.getModel(), d.getOsName(), d.getOsVersion(),
                d.getAppVersion(), d.getGpsStatus(), d.getStatus(), d.getLastAccess(), d.getCreatedAt(),
                d.getFcmToken() != null && !d.getFcmToken().isBlank());
    }

    /** Dispositivo con su dueña. No expone el token FCM, solo si existe. */
    public record AdminDeviceView(Long id, Long accountId, Long userId, String ownerName, String ownerEmail,
                                  String ownerPhone, String deviceUuid, String brand, String model,
                                  String osName, String osVersion, String appVersion, String gpsStatus,
                                  String status, LocalDateTime lastAccess, LocalDateTime createdAt,
                                  boolean pushTokenRegistered) {}
}
