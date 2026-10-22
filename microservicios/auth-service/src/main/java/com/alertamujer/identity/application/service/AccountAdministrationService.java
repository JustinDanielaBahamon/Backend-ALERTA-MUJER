package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.ManageAccountsUseCase;
import com.alertamujer.identity.application.port.out.HashingPort;
import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.IdentityEventsPort;
import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.application.port.out.SessionRepository;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.AccountChanges;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Administración de cuentas: los datos y sus reglas viven en identity; el panel (admin) los usa. */
@Service
public class AccountAdministrationService implements ManageAccountsUseCase {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final ProfileRepository profiles;
    private final SessionRepository sessions;
    private final HashingPort hashing;
    private final IdentityEventsPort events;

    public AccountAdministrationService(UserRepository users, AccountRepository accounts, ProfileRepository profiles,
                                        SessionRepository sessions, HashingPort hashing, IdentityEventsPort events) {
        this.users = users;
        this.accounts = accounts;
        this.profiles = profiles;
        this.sessions = sessions;
        this.hashing = hashing;
        this.events = events;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> listAll() {
        Map<Long, Account> byUser = accounts.all().stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity(), (a, b) -> a));
        Map<Long, Long> profileByUser = profiles.all().stream()
                .collect(Collectors.toMap(UserProfile::getUserId, UserProfile::getId, (a, b) -> a));
        return users.all().stream()
                .map(u -> new UserAccount(u, byUser.get(u.getId()), profileByUser.get(u.getId())))
                .toList();
    }

    @Override
    @Transactional
    public UserAccount update(Long userId, AccountChanges changes, Long actor) {
        User user = existing(userId);
        Account account = accounts.ofUser(userId).orElse(null);
        if (changes.accountStatus() != null) {
            if (!Account.STATUSES.contains(changes.accountStatus())) {
                throw new IllegalArgumentException("Invalid account status. Must be one of: active, inactive, blocked, pending");
            }
            if (userId.equals(actor) && "blocked".equals(changes.accountStatus())) {
                throw new IllegalArgumentException("Cannot block your own account");
            }
            if (account != null) {
                account.changeStatus(changes.accountStatus());
                accounts.save(account);
            }
        }
        if (changes.email() != null) {
            Optional<User> other = users.byEmail(changes.email());
            if (other.isPresent() && !other.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
        }
        user.updateFromAdministration(changes.firstName(), changes.lastName(), changes.telephone(), changes.email());
        users.save(user);
        return new UserAccount(user, account, profiles.ofUser(userId).map(UserProfile::getId).orElse(null));
    }

    @Override
    @Transactional
    public void anonymize(Long userId) {
        User user = existing(userId);
        user.anonymize();
        users.save(user);
        Optional<UserProfile> profile = profiles.ofUser(userId);
        profile.ifPresent(p -> {
            p.setProfilePhotoUrl(null);
            profiles.save(p);
        });
        accounts.ofUser(userId).ifPresent(account -> {
            // Contraseña aleatoria que nadie conoce: no se puede volver a entrar
            account.deactivate(hashing.hash(UUID.randomUUID().toString()));
            accounts.save(account);
            sessions.activeForAccount(account.getId()).forEach(s -> {
                s.revoke();
                sessions.save(s);
            });
        });
        // Contactos, ubicaciones, recorridos y grabaciones viven en Alert y Evidence: se avisa para que los borren
        events.accountDeleted(userId, profile.map(UserProfile::getId).orElse(null));
    }

    private User existing(Long userId) {
        return users.find(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }
}
