package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.RegistrationData;
import com.alertamujer.identity.application.port.in.SignInUseCase;
import com.alertamujer.identity.application.port.in.RegisterUserUseCase;
import com.alertamujer.identity.application.port.in.Session;
import com.alertamujer.identity.application.port.out.HashingPort;
import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.IdentityEventsPort;
import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.application.port.out.TokensPort;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.application.port.out.GoogleVerifierPort;
import com.alertamujer.identity.application.port.out.GoogleVerifierPort.GoogleIdentity;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Registration, sign-in (email or Google), refresh and sign-out. Only knows ports. */
@Service
public class SessionService implements RegisterUserUseCase, SignInUseCase {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final ProfileRepository profiles;
    private final HashingPort hashing;
    private final TokensPort tokens;
    private final GoogleVerifierPort google;
    private final IdentityEventsPort events;

    public SessionService(UserRepository users, AccountRepository accounts, ProfileRepository profiles,
                         HashingPort hashing, TokensPort tokens, GoogleVerifierPort google,
                         IdentityEventsPort events) {
        this.users = users;
        this.accounts = accounts;
        this.profiles = profiles;
        this.hashing = hashing;
        this.tokens = tokens;
        this.google = google;
        this.events = events;
    }

    // ── Registro ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Session registrar(RegistrationData d) {
        if (Boolean.FALSE.equals(d.acceptsTerms())) {
            throw new IllegalArgumentException("Debes aceptar los términos y la política de privacidad");
        }
        String telephone = User.cleanPhone(d.telephone());
        String document = User.emptyToNull(d.documentNumber());
        Map<String, String> repeated = new LinkedHashMap<>();
        if (users.emailExists(d.email())) repeated.put("email", "Este correo ya está registrado");
        if (telephone != null && users.phoneExists(telephone)) {
            repeated.put("telefono", "Este número de teléfono ya está registrado");
        }
        if (document != null && users.documentExists(document)) {
            repeated.put("numeroDocumento", "Este número de documento ya está registrado");
        }
        if (!repeated.isEmpty()) throw new DuplicateFieldsException(repeated);

        users.ensureUserRole();
        User newOne = User.newOne(d.name(), d.email(), d.telephone(), d.documentType(),
                d.documentNumber(), User.formDate(d.birthDate()));
        // Only recorded if the checkbox arrived checked (older apps do not send it)
        if (Boolean.TRUE.equals(d.acceptsTerms())) newOne.acceptTerms(LocalDateTime.now());
        User user = users.save(newOne);
        accounts.save(Account.newOne(user.getId(), hashing.hash(d.password())));
        // Without a profile she cannot create alerts, contacts or locations
        if (!profiles.existsForUser(user.getId())) profiles.save(UserProfile.of(user.getId()));
        return open(user);
    }

    // ── Sign-in ────────────────────────────────────────────

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public Session withEmail(String email, String password) {
        User user = users.byEmail(email).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        Account account = accountOf(user);
        if (account.blocked()) {
            audit(user, account, AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada");
            throw new InvalidCredentialsException("Account blocked");
        }
        if (!hashing.coincide(password, account.getPasswordHash())) {
            audit(user, account, AdminSessionEvent.LOGIN_FAILED, "Contraseña incorrecta");
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return signIn(user, account, "Con correo y contraseña");
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public Session conGoogle(String idToken) {
        GoogleIdentity id = google.verify(idToken);
        User user = users.byEmail(id.email()).orElseGet(() -> createFromGoogle(id));
        Account account = accountOf(user);
        if (account.blocked()) {
            audit(user, account, AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada (Google)");
            throw new InvalidCredentialsException("Account blocked");
        }
        return signIn(user, account, "Con Google");
    }

    @Override
    public String refresh(String refreshToken) {
        try {
            User user = users.find(tokens.userForRefresh(refreshToken)).orElseThrow();
            if (!accountOf(user).canRefreshSession()) throw new InvalidCredentialsException("Account blocked");
            return tokens.access(user.getId(), user.roleOfToken());
        } catch (RuntimeException e) {
            // Same message for any reason: no hints about the token
            throw new InvalidCredentialsException("Refresh token inválido o expirado");
        }
    }

    @Override
    public void close(Long userId, String reason) {
        if (userId == null) return;
        users.find(userId).filter(User::esAdmin).ifPresent(u -> accounts.ofUser(userId).ifPresent(c ->
                events.adminSession(c.getId(), AdminSessionEvent.LOGOUT, "inactividad".equals(reason) ? "Por inactividad" : "Voluntario")));
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Session signIn(User user, Account account, String como) {
        account.recordAccess(LocalDateTime.now());
        accounts.save(account);
        audit(user, account, AdminSessionEvent.LOGIN_SUCCESS, como);
        return open(user);
    }

    private Session open(User user) {
        return new Session(tokens.access(user.getId(), user.roleOfToken()), tokens.refresh(user.getId()), user);
    }

    private User createFromGoogle(GoogleIdentity id) {
        users.ensureUserRole();
        User newOne = User.fromGoogle(id.email(), id.name(), id.givenName(), id.familyName());
        // The sign-in screen warns that continuing with Google means accepting the terms
        newOne.acceptTerms(LocalDateTime.now());
        User user = users.save(newOne);
        // Random password impossible to guess: the account is used with Google
        accounts.save(Account.newOne(user.getId(), hashing.hash(UUID.randomUUID() + UUID.randomUUID().toString())));
        profiles.save(UserProfile.of(user.getId()));
        return user;
    }

    private Account accountOf(User user) {
        return accounts.ofUser(user.getId()).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private void audit(User user, Account account, String action, String detail) {
        if (user.esAdmin()) events.adminSession(account.getId(), action, detail);
    }
}
