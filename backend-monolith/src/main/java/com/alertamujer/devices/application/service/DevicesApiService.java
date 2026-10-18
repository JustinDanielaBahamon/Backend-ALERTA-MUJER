package com.alertamujer.devices.application.service;

import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DevicesApiService implements DevicesApi {

    private final DeviceRepository deviceRepository;
    private final IdentityApi identityApi;

    public DevicesApiService(DeviceRepository deviceRepository, IdentityApi identityApi) {
        this.deviceRepository = deviceRepository;
        this.identityApi = identityApi;
    }

    /**
     * Los dispositivos se guardan por account.id, no por user_profile.id: se traduce
     * perfil -> usuaria -> cuenta (antes se buscaba con el id de perfil como si fuera el de cuenta).
     */
    @Override
    public List<String> pushTokensOfProfile(Long userProfileId) {
        Optional<Long> cuenta = identityApi.findUserByProfileId(userProfileId)
                .map(UserSummary::id)
                .map(identityApi::requireAccountId);
        return cuenta.map(this::tokensDeCuenta).orElse(List.of());
    }

    @Override
    public List<String> pushTokensOfUser(Long userId) {
        try {
            return tokensDeCuenta(identityApi.requireAccountId(userId));
        } catch (com.alertamujer.shared.exception.ResourceNotFoundException e) {
            return List.of();
        }
    }

    @Override
    public List<String> phonePushTokensOfUser(Long userId) {
        try {
            return deviceRepository.findByAccountId(identityApi.requireAccountId(userId)).stream()
                    .filter(d -> !OS_WEB.equalsIgnoreCase(d.getOsName()))
                    .map(Device::getFcmToken)
                    .filter(token -> token != null && !token.isBlank())
                    .toList();
        } catch (com.alertamujer.shared.exception.ResourceNotFoundException e) {
            return List.of();
        }
    }

    /** osName con el que se registra el panel web (recibe notificaciones del navegador). */
    public static final String OS_WEB = "web";

    private List<String> tokensDeCuenta(Long accountId) {
        return deviceRepository.findByAccountId(accountId).stream()
                .map(Device::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();
    }

    @Override
    public long countDevices() {
        return deviceRepository.count();
    }
}
