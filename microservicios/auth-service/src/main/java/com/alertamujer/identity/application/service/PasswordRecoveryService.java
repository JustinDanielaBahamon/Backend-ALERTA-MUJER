package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.RecoverPasswordUseCase;
import com.alertamujer.identity.application.port.out.HashingPort;
import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.IdentityEventsPort;
import com.alertamujer.identity.application.port.out.RecoveryRepository;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Recuperación de contraseña con un código de 6 dígitos enviado al correo (web y app móvil). */
@Service
public class PasswordRecoveryService implements RecoverPasswordUseCase {

    static final String INVALID_CODE = "El código no es válido o ya venció. Pide uno nuevo.";

    private final UserRepository users;
    private final AccountRepository accounts;
    private final RecoveryRepository solicitudes;
    private final HashingPort hashing;
    private final IdentityEventsPort events;
    private final SecureRandom random = new SecureRandom();

    public PasswordRecoveryService(UserRepository users, AccountRepository accounts,
                                         RecoveryRepository solicitudes, HashingPort hashing,
                                         IdentityEventsPort events) {
        this.users = users;
        this.accounts = accounts;
        this.solicitudes = solicitudes;
        this.hashing = hashing;
        this.events = events;
    }

    @Override
    @Transactional
    public void request(String email, String ip) {
        Optional<User> user = users.byEmail(normalize(email));
        Optional<Account> account = user.flatMap(u -> accounts.ofUser(u.getId()));
        if (account.isEmpty() || account.get().blocked()) return;
        Long accountId = account.get().getId();
        LocalDateTime now = LocalDateTime.now();

        List<RecoveryRequest> pendingOnes = solicitudes.pendingForAccount(accountId);
        if (pendingOnes.stream().anyMatch(r -> r.recent(now))) return;
        pendingOnes.forEach(RecoveryRequest::replace);
        solicitudes.saveAll(pendingOnes);

        String code;
        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (solicitudes.hashExists(RecoveryRequest.hash(accountId, code)));
        solicitudes.save(RecoveryRequest.newOne(accountId, code, ip, now));

        User u = user.get();
        events.recoveryCode(u.getEmail(), u.fullName(), code, RecoveryRequest.VALIDITY_MINUTES);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verify(String email, String code) {
        validate(email, code);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void reset(String email, String code, String newPassword) {
        Account.validateNewPassword(newPassword);
        RecoveryRequest deletionRequest = validate(email, code);
        Account account = accounts.find(deletionRequest.getAccountId()).orElseThrow(() -> new IllegalArgumentException(INVALID_CODE));
        account.changePassword(hashing.hash(newPassword));
        accounts.save(account);
        deletionRequest.use(LocalDateTime.now());
        solicitudes.save(deletionRequest);
        if (users.find(account.getUserId()).map(User::esAdmin).orElse(false)) {
            events.adminSession(account.getId(), AdminSessionEvent.PASSWORD_CHANGED, "Recuperación por correo");
        }
    }

    private RecoveryRequest validate(String email, String code) {
        if (code == null || !code.matches("\\d{6}")) throw new IllegalArgumentException(INVALID_CODE);
        Account account = users.byEmail(normalize(email))
                .flatMap(u -> accounts.ofUser(u.getId()))
                .filter(a -> !a.blocked())
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CODE));
        RecoveryRequest deletionRequest = solicitudes.pendingForAccount(account.getId()).stream()
                .max(Comparator.comparing(RecoveryRequest::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CODE));
        if (deletionRequest.expired(LocalDateTime.now())) {
            deletionRequest.expire();
            solicitudes.save(deletionRequest);
            throw new IllegalArgumentException(INVALID_CODE);
        }
        if (!deletionRequest.coincide(code)) {
            deletionRequest.recordFailure();
            solicitudes.save(deletionRequest);
            throw new IllegalArgumentException(INVALID_CODE);
        }
        return deletionRequest;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim();
    }
}
