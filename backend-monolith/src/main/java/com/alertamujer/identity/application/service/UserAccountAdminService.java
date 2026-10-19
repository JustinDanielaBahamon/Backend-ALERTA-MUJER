package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.DeviceSessionRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Administración de cuentas (antes en el módulo admin): los datos y sus reglas viven en identity. */
@Service
public class UserAccountAdminService implements IdentityAdminApi {

    private static final Set<String> ESTADOS = Set.of("active", "inactive", "blocked", "pending");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAccountAdminService(UserRepository userRepository, AccountRepository accountRepository,
                                   UserProfileRepository userProfileRepository,
                                   DeviceSessionRepository deviceSessionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.deviceSessionRepository = deviceSessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountView> listUserAccounts() {
        Map<Long, Account> cuentas = accountRepository.findAll().stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity(), (a, b) -> a));
        Map<Long, Long> perfiles = userProfileRepository.findAll().stream()
                .collect(Collectors.toMap(UserProfile::getUserId, UserProfile::getId, (a, b) -> a));
        return userRepository.findAll().stream()
                .map(u -> vista(u, cuentas.get(u.getId()), perfiles.get(u.getId())))
                .toList();
    }

    @Override
    @Transactional
    public UserAccountView updateUserAccount(Long userId, UserAccountChanges cambios, Long actingUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Account account = accountRepository.findByUserId(userId).orElse(null);

        if (cambios.accountStatus() != null) {
            if (!ESTADOS.contains(cambios.accountStatus())) {
                throw new IllegalArgumentException(
                        "Invalid account status. Must be one of: active, inactive, blocked, pending");
            }
            if (userId.equals(actingUserId) && "blocked".equals(cambios.accountStatus())) {
                throw new IllegalArgumentException("Cannot block your own account");
            }
            if (account != null) {
                account.setStatus(cambios.accountStatus());
                accountRepository.save(account);
            }
        }

        if (cambios.firstName() != null) user.setFirstName(cambios.firstName());
        if (cambios.lastName() != null) user.setLastName(cambios.lastName());
        if (cambios.telephone() != null) user.setTelephone(cambios.telephone());
        if (cambios.email() != null) {
            Optional<User> otra = userRepository.findByEmail(cambios.email());
            if (otra.isPresent() && !otra.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(cambios.email());
        }
        userRepository.save(user);

        Long perfil = userProfileRepository.findByUserId(userId).map(UserProfile::getId).orElse(null);
        return vista(user, account, perfil);
    }

    @Override
    @Transactional
    public void anonymizeAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setFirstName("Cuenta");
        user.setLastName("eliminada");
        user.setEmail("eliminada-" + userId + "@alertamujer.invalid");
        user.setTelephone(null);
        user.setDocumentNumber(null);
        user.setDocumentType(null);
        user.setBirthdate(null);
        userRepository.save(user);
        userProfileRepository.findByUserId(userId).ifPresent(p -> {
            p.setProfilePhotoUrl(null);
            userProfileRepository.save(p);
        });
        accountRepository.findByUserId(userId).ifPresent(account -> {
            account.setStatus("inactive");
            // Contraseña aleatoria que nadie conoce: no se puede volver a entrar
            account.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            accountRepository.save(account);
            deviceSessionRepository.findByAccountIdAndStatus(account.getId(), "active").forEach(sesion -> {
                sesion.setStatus("revoked");
                deviceSessionRepository.save(sesion);
            });
        });
    }

    private static UserAccountView vista(User u, Account a, Long profileId) {
        return new UserAccountView(u.getId(), profileId, u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getTelephone(), u.getRole() != null ? u.getRole().getName() : null,
                a != null ? a.getStatus() : null, u.getCreatedAt(), a != null ? a.getLastAccess() : null);
    }
}
