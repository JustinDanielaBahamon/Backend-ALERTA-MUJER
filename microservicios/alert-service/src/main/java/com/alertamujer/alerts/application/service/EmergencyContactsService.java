package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.EmergencyContactsUseCase;
import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.ContactRepository;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.ContactData;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Emergency contacts: each user manages her own, up to the configured maximum. */
@Service
public class EmergencyContactsService implements EmergencyContactsUseCase {

    private final ContactRepository repository;
    private final IdentityPort identity;
    private final SettingsPort settings;
    private final PhonesPort phones;

    public EmergencyContactsService(ContactRepository repository, IdentityPort identity, SettingsPort settings,
                                      PhonesPort phones) {
        this.repository = repository;
        this.identity = identity;
        this.settings = settings;
        this.phones = phones;
    }

    @Override
    public Optional<EmergencyContact> one(Long id, Long userId) {
        Long profile = identity.profileOf(userId).orElse(null);
        if (profile == null) return Optional.empty();
        return repository.find(id).filter(c -> c.esDe(profile));
    }

    @Override
    public List<EmergencyContact> ofUser(Long requestedId, Long userId) {
        Long own = profileOf(userId);
        if (requestedId.equals(userId) || requestedId.equals(own)) {
            return repository.ofUser(own);
        }
        // Contacts are third-party data: not even the administration queries them (audit S2)
        throw new AccessDeniedException("No puedes ver los contactos de otra usuaria");
    }

    @Override
    @Transactional
    public EmergencyContact create(ContactData data, Long userId) {
        // The owner comes from the JWT, never from the body (prevents IDOR)
        Long profile = profileOf(userId);
        int max = settings.maxContacts();
        if (repository.ofUser(profile).size() >= max) {
            throw new IllegalArgumentException("Puedes tener como máximo " + max + " contactos de emergencia");
        }
        return repository.save(EmergencyContact.created(profile, data));
    }

    @Override
    @Transactional
    public EmergencyContact update(Long id, ContactData data, Long userId) {
        EmergencyContact contact = own(id, userId, "No puedes modificar contactos de otro usuario");
        contact.update(data);
        return repository.save(contact);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        own(id, userId, "No puedes eliminar contactos de otro usuario");
        repository.delete(id);
    }

    @Override
    public Map<Long, String> appStatus(Long userId) {
        Map<Long, String> statuses = new LinkedHashMap<>();
        for (EmergencyContact c : repository.ofUser(profileOf(userId))) {
            String email = c.getEmail();
            Optional<Long> account = email == null || email.isBlank()
                    ? Optional.empty()
                    : identity.userByEmail(email.trim());
            statuses.put(c.getId(), account
                    .map(id -> phones.phoneTokens(id).isEmpty() ? NO_PHONE : CON_APP)
                    .orElse(NO_ACCOUNT));
        }
        return statuses;
    }

    private Long profileOf(Long userId) {
        return identity.profileOf(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private EmergencyContact own(Long id, Long userId, String foreignMessage) {
        EmergencyContact contact = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        if (!contact.esDe(profileOf(userId))) throw new AccessDeniedException(foreignMessage);
        return contact;
    }
}
