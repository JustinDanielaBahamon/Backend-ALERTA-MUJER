package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase;
import com.alertamujer.alerts.application.port.out.ContactRepository;
import com.alertamujer.alerts.application.port.out.RecipientRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertContact;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Destinatarios de una alerta: qué contactos se avisan, por qué canal (correo si tiene correo, y
 * push si además es usuaria de la app) y en qué estado está cada envío.
 */
@Service
public class RecipientsService implements AlertRecipientsUseCase {

    private final RecipientRepository repository;
    private final ContactRepository contacts;
    private final IdentityPort identity;
    private final AlertEventsPort events;

    public RecipientsService(RecipientRepository repository, ContactRepository contacts,
                                IdentityPort identity, AlertEventsPort events) {
        this.repository = repository;
        this.contacts = contacts;
        this.identity = identity;
        this.events = events;
    }

    /**
     * Crea un registro por cada contacto avisado y canal disponible. Sin contactIds se avisa a todos
     * los contactos de la usuaria; con contactIds, solo a esos (deben ser suyos: un id ajeno o borrado
     * cancela la alerta en vez de avisar a otras personas).
     */
    @Transactional
    public void registrar(Alert alert, List<Long> contactIds) {
        List<EmergencyContact> notified = contacts.ofUser(alert.getUserProfileId());
        if (contactIds != null && !contactIds.isEmpty()) {
            Set<Long> chosen = contactIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            notified = notified.stream().filter(c -> chosen.contains(c.getId())).toList();
            if (notified.size() != chosen.size()) {
                throw new IllegalArgumentException("Alguno de los contactos elegidos no existe");
            }
        }
        List<AlertContact> records = new ArrayList<>();
        for (EmergencyContact contact : notified) {
            String email = contact.emailToNotify();
            if (email.isEmpty()) continue;
            records.add(AlertContact.created(alert.getId(), contact.getId(), AlertContact.EMAIL_CHANNEL, email));
            if (identity.hasAccount(email)) {
                records.add(AlertContact.created(alert.getId(), contact.getId(), AlertContact.PUSH_CHANNEL, "App Alerta Mujer"));
            }
        }
        repository.saveAll(records);
        // Los avisos salen después de confirmar la transacción (módulo de notificaciones)
        events.alertActivated(alert);
    }

    @Override
    public List<NotifiedContact> contactsOf(Alert alert) {
        boolean ended = alert.ended();
        Map<Long, EmergencyContact> byId = new LinkedHashMap<>();
        contacts.ofUser(alert.getUserProfileId()).forEach(c -> byId.put(c.getId(), c));

        Map<Long, List<DeliveryChannel>> channels = new LinkedHashMap<>();
        for (AlertContact d : repository.ofAlert(alert.getId())) {
            channels.computeIfAbsent(d.getEmergencyContactId(), k -> new ArrayList<>())
                    .add(new DeliveryChannel(d.getChannel(), d.getDestination(), d.visibleStatus(ended),
                            d.getSentAt(), d.getDeliveredAt()));
        }
        List<NotifiedContact> result = new ArrayList<>();
        channels.forEach((contactId, list) -> {
            EmergencyContact c = byId.get(contactId);
            result.add(new NotifiedContact(contactId,
                    c != null ? c.getContactName() : contactId == null ? "Contacto eliminado" : "Contacto",
                    c != null ? c.getRelationship() : null,
                    list));
        });
        return result;
    }

    @Override
    public List<AlertRecipient> recipients(Long alertId) {
        List<AlertContact> deliveries = repository.ofAlert(alertId);
        Map<Long, String> names = contacts.findSeveral(deliveries.stream()
                        .map(AlertContact::getEmergencyContactId).filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(EmergencyContact::getId,
                        c -> c.getContactName() == null ? "" : c.getContactName(), (a, b) -> a));
        return deliveries.stream()
                .map(e -> new AlertRecipient(e.getId(), e.getEmergencyContactId(), names.get(e.getEmergencyContactId()),
                        e.getChannel(), e.getDestination()))
                .toList();
    }

    @Override
    @Transactional
    public void recordDelivery(Long recipientId, String status, String error) {
        repository.find(recipientId).ifPresent(d -> {
            d.recordAttempt(status, error, LocalDateTime.now());
            repository.save(d);
        });
    }

    @Override
    public List<Long> alertsNotifiedTo(String email) {
        if (email == null || email.isBlank()) return List.of();
        return repository.alertsNotifiedTo(email.trim());
    }
}
