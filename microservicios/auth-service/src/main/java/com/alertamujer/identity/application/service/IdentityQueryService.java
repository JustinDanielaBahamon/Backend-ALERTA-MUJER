package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.QueryIdentityUseCase;
import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Queries of people, profiles and accounts for the other modules and microservices. */
@Service
@Transactional(readOnly = true)
public class IdentityQueryService implements QueryIdentityUseCase {

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final AccountRepository accounts;

    public IdentityQueryService(UserRepository users, ProfileRepository profiles, AccountRepository accounts) {
        this.users = users;
        this.profiles = profiles;
        this.accounts = accounts;
    }

    @Override
    public Optional<Long> profileOf(Long userId) {
        return profiles.ofUser(userId).map(UserProfile::getId);
    }

    @Override
    public Optional<Long> accountOf(Long userId) {
        return accounts.ofUser(userId).map(Account::getId);
    }

    @Override
    public Optional<User> user(Long userId) {
        return userId == null ? Optional.empty() : users.find(userId);
    }

    @Override
    public Optional<User> byEmail(String email) {
        return email == null ? Optional.empty() : users.byEmail(email);
    }

    @Override
    public Optional<User> byProfile(Long profileId) {
        return profileId == null ? Optional.empty() : profiles.find(profileId).flatMap(p -> user(p.getUserId()));
    }

    @Override
    public Optional<User> byAccount(Long accountId) {
        return accountId == null ? Optional.empty() : accounts.find(accountId).flatMap(c -> user(c.getUserId()));
    }

    @Override
    public Map<Long, User> byId() {
        return users.all().stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }

    @Override
    public Map<Long, User> byProfileId() {
        Map<Long, User> byUser = byId();
        return profiles.all().stream()
                .filter(p -> byUser.containsKey(p.getUserId()))
                .collect(Collectors.toMap(UserProfile::getId, p -> byUser.get(p.getUserId()), (a, b) -> a));
    }

    @Override
    public Map<Long, User> byAccountId() {
        Map<Long, User> byUser = byId();
        return accounts.all().stream()
                .filter(c -> byUser.containsKey(c.getUserId()))
                .collect(Collectors.toMap(Account::getId, c -> byUser.get(c.getUserId()), (a, b) -> a));
    }
}
