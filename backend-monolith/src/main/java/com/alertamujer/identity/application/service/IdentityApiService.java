package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Implementación de la API pública de identity sobre sus propios repositorios. */
@Service
@Transactional(readOnly = true)
public class IdentityApiService implements IdentityApi {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccountRepository accountRepository;

    public IdentityApiService(UserRepository userRepository, UserProfileRepository userProfileRepository,
                              AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Long requireProfileId(Long userId) {
        return findProfileId(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    @Override
    public Optional<Long> findProfileId(Long userId) {
        return userProfileRepository.findByUserId(userId).map(UserProfile::getId);
    }

    @Override
    public Long requireAccountId(Long userId) {
        return accountRepository.findByUserId(userId).map(Account::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Account for user", userId));
    }

    @Override
    public Optional<UserSummary> findUser(Long userId) {
        return userId == null ? Optional.empty() : userRepository.findById(userId).map(IdentityApiService::resumen);
    }

    @Override
    public Optional<UserSummary> findUserByEmail(String email) {
        return email == null ? Optional.empty() : userRepository.findByEmail(email).map(IdentityApiService::resumen);
    }

    @Override
    public Optional<UserSummary> findUserByProfileId(Long profileId) {
        return profileId == null ? Optional.empty()
                : userProfileRepository.findById(profileId).flatMap(p -> findUser(p.getUserId()));
    }

    @Override
    public Optional<UserSummary> findUserByAccountId(Long accountId) {
        return accountId == null ? Optional.empty()
                : accountRepository.findById(accountId).flatMap(a -> findUser(a.getUserId()));
    }

    @Override
    public Map<Long, UserSummary> usersById() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, IdentityApiService::resumen, (a, b) -> a));
    }

    @Override
    public Map<Long, UserSummary> usersByProfileId() {
        Map<Long, UserSummary> usuarios = usersById();
        return userProfileRepository.findAll().stream()
                .filter(p -> usuarios.containsKey(p.getUserId()))
                .collect(Collectors.toMap(UserProfile::getId, p -> usuarios.get(p.getUserId()), (a, b) -> a));
    }

    @Override
    public Map<Long, UserSummary> usersByAccountId() {
        Map<Long, UserSummary> usuarios = usersById();
        return accountRepository.findAll().stream()
                .filter(a -> usuarios.containsKey(a.getUserId()))
                .collect(Collectors.toMap(Account::getId, a -> usuarios.get(a.getUserId()), (a, b) -> a));
    }

    static UserSummary resumen(User u) {
        return new UserSummary(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getTelephone(),
                u.getRole() == null ? null : u.getRole().getId(), u.getCreatedAt());
    }
}
