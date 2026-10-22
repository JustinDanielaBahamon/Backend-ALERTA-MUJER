package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.ManageAccountsUseCase;
import com.alertamujer.identity.application.port.in.MyAccountUseCase;
import com.alertamujer.identity.application.port.out.HashingPort;
import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.application.port.out.IdentityEventsPort;
import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.UserChanges;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.PhotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Datos, foto y contraseña de la persona en sesión. */
@Service
public class MyAccountService implements MyAccountUseCase {

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final AccountRepository accounts;
    private final HashingPort hashing;
    private final IdentityEventsPort events;
    private final ManageAccountsUseCase administration;

    public MyAccountService(UserRepository users, ProfileRepository profiles, AccountRepository accounts,
                           HashingPort hashing, IdentityEventsPort events, ManageAccountsUseCase administration) {
        this.users = users;
        this.profiles = profiles;
        this.accounts = accounts;
        this.hashing = hashing;
        this.events = events;
        this.administration = administration;
    }

    @Override
    public UserWithPhoto mia(Long userId) {
        return new UserWithPhoto(existing(userId), photo(userId));
    }

    @Override
    @Transactional
    public UserWithPhoto update(Long userId, UserChanges changes) {
        User user = existing(userId);
        changes = distinct(user, changes);
        // La foto se valida antes de guardar nada: "" o null la quitan
        if (changes.profilePhoto() != null) {
            String valid = PhotoDataUri.validate(changes.profilePhoto().orElse(null));
            profiles.ofUser(userId).ifPresent(p -> {
                p.setProfilePhotoUrl(valid);
                profiles.save(p);
            });
        }
        user.updateData(changes);
        return new UserWithPhoto(users.save(user), photo(userId));
    }

    /**
     * El teléfono y el documento identifican a una sola persona (así se registró): al cambiarlos o al
     * completarlos (cuentas creadas con Google) no pueden ser los de otra cuenta.
     */
    private UserChanges distinct(User user, UserChanges c) {
        Optional<String> telephone = c.telephone() == null ? null : c.telephone().map(User::cleanPhone);
        Optional<String> document = c.documentNumber() == null ? null : c.documentNumber().map(User::emptyToNull);
        Map<String, String> repeated = new LinkedHashMap<>();
        if (telephone != null && telephone.isPresent() && !telephone.get().equals(user.getTelephone())
                && users.phoneExists(telephone.get())) {
            repeated.put("telephone", "Este número de teléfono ya está registrado");
        }
        if (document != null && document.isPresent() && !document.get().equals(user.getDocumentNumber())
                && users.documentExists(document.get())) {
            repeated.put("documentNumber", "Este número de documento ya está registrado");
        }
        if (!repeated.isEmpty()) throw new DuplicateFieldsException(repeated);
        return new UserChanges(c.firstName(), c.lastName(), telephone, document, c.documentType(), c.birthdate(),
                c.profilePhoto());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String current, String newOne) {
        Account account = accounts.ofUser(userId).orElseThrow(() -> new IllegalArgumentException("La cuenta no existe"));
        if (current == null || !hashing.coincide(current, account.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        Account.validateNewPassword(newOne);
        if (hashing.coincide(newOne, account.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }
        account.changePassword(hashing.hash(newOne));
        accounts.save(account);
        if (users.find(userId).map(User::esAdmin).orElse(false)) {
            events.adminSession(account.getId(), AdminSessionEvent.PASSWORD_CHANGED, null);
        }
    }

    @Override
    @Transactional
    public void deleteMyAccount(Long userId, String confirmation) {
        if (confirmation == null || !DELETE_CONFIRMATION.equalsIgnoreCase(confirmation.trim())) {
            throw new IllegalArgumentException("Escribe " + DELETE_CONFIRMATION + " para confirmar");
        }
        if (existing(userId).esAdmin()) {
            throw new IllegalArgumentException(
                    "Las cuentas de administración se eliminan desde el panel, con la aprobación de otra administradora");
        }
        // Mismo borrado que aprueba la administración: datos personales fuera, sesión cerrada y aviso a los demás servicios
        administration.anonymize(userId);
    }

    private User existing(Long userId) {
        return users.find(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private String photo(Long userId) {
        return profiles.ofUser(userId).map(UserProfile::getProfilePhotoUrl).orElse(null);
    }
}
