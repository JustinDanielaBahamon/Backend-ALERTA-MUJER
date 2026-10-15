package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alertas para el panel de administración: los campos de la alerta más la usuaria que la
 * activó (nombre y correo), que la entidad Alert no trae.
 */
@Service
public class AdminAlertService {

    private final AlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public AdminAlertService(AlertRepository alertRepository,
                             UserProfileRepository userProfileRepository,
                             UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    /** Todas las alertas, de la más reciente a la más antigua. */
    @Transactional(readOnly = true)
    public List<AdminAlertView> listar() {
        Map<Long, Long> usuarioPorPerfil = userProfileRepository.findAll().stream()
                .collect(Collectors.toMap(UserProfile::getId, UserProfile::getUserId, (a, b) -> a));
        Map<Long, User> usuarios = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return alertRepository.findAll().stream()
                .sorted(Comparator.comparing(Alert::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    Long userId = usuarioPorPerfil.get(a.getUserProfileId());
                    User u = userId != null ? usuarios.get(userId) : null;
                    String nombre = u == null ? null
                            : ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                               + (u.getLastName() == null ? "" : u.getLastName())).trim();
                    return new AdminAlertView(a.getId(), a.getUserProfileId(), userId,
                            nombre, u != null ? u.getEmail() : null,
                            a.getDeviceId(), a.getAlertType(), a.getActivationMethod(), a.getStatus(),
                            a.getMessage(), a.getLatitude(), a.getLongitude(),
                            a.getStartedAt(), a.getEndedAt(), a.getCancelledAt(), a.getCreatedAt());
                })
                .toList();
    }

    /** Mismos campos que Alert (los clientes existentes siguen funcionando) más userId, userName y userEmail. */
    public record AdminAlertView(Long id, Long userProfileId, Long userId, String userName, String userEmail,
                                 Long deviceId, String alertType, String activationMethod, String status,
                                 String message, BigDecimal latitude, BigDecimal longitude,
                                 LocalDateTime startedAt, LocalDateTime endedAt, LocalDateTime cancelledAt,
                                 LocalDateTime createdAt) {}
}
