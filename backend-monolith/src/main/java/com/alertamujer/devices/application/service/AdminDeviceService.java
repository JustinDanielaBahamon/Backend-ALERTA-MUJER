package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dispositivos para el panel de administración: datos del celular más la usuaria dueña
 * (la entidad Device solo guarda accountId). La administradora puede ver, cambiar el estado
 * y borrar cualquier dispositivo; los demás endpoints solo dejan hacerlo a la dueña.
 */
@Service
public class AdminDeviceService {

    private static final Set<String> ESTADOS = Set.of("active", "inactive", "blocked", "lost");

    private final DeviceRepository deviceRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AdminDeviceService(DeviceRepository deviceRepository, AccountRepository accountRepository,
                              UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminDeviceView> listar() {
        Map<Long, Long> usuarioPorCuenta = accountRepository.findAll().stream()
                .collect(Collectors.toMap(Account::getId, Account::getUserId, (a, b) -> a));
        Map<Long, User> usuarios = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(Device::getLastAccess, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(d -> vista(d, usuarios.get(usuarioPorCuenta.get(d.getAccountId()))))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDeviceView obtener(Long id) {
        Device d = buscar(id);
        User dueña = accountRepository.findById(d.getAccountId())
                .flatMap(a -> userRepository.findById(a.getUserId()))
                .orElse(null);
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

    private AdminDeviceView vista(Device d, User u) {
        String nombre = u == null ? null
                : ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                   + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return new AdminDeviceView(d.getId(), d.getAccountId(), u != null ? u.getId() : null,
                nombre, u != null ? u.getEmail() : null, u != null ? u.getTelephone() : null,
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
