package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertContact;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.AlertContactRepository;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.identity.api.IdentityApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Destinatarios de una alerta: qué contactos se deben avisar, por qué canal y en qué estado
 * está cada envío (tabla alert.alert_contact).
 *
 * Canales previstos: correo (si el contacto tiene correo) y notificación push (si el contacto
 * también es usuaria registrada de la app). Los envíos reales aún no existen, así que los
 * registros quedan "pending" hasta que un emisor los marque como enviados o entregados.
 */
@Service
public class AlertContactService {

    public static final String CANAL_CORREO = "email";
    public static final String CANAL_PUSH = "push";

    private static final Set<String> ESTADOS_ABIERTOS = Set.of("pending", "sent");
    private static final Set<String> ALERTA_TERMINADA = Set.of("cancelled", "resolved", "finished", "ended");

    private final AlertContactRepository alertContactRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final IdentityApi identityApi;
    private final ApplicationEventPublisher events;

    public AlertContactService(AlertContactRepository alertContactRepository,
                               EmergencyContactRepository emergencyContactRepository,
                               IdentityApi identityApi,
                               ApplicationEventPublisher events) {
        this.alertContactRepository = alertContactRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.identityApi = identityApi;
        this.events = events;
    }

    /** Crea un registro por cada contacto de la usuaria y cada canal disponible para ese contacto. */
    @Transactional
    public void registrarDestinatarios(Alert alert) {
        List<AlertContact> registros = new ArrayList<>();
        for (EmergencyContact contacto : emergencyContactRepository.findByUserProfileId(alert.getUserProfileId())) {
            String correo = contacto.getEmail() == null ? "" : contacto.getEmail().trim();
            if (!correo.isEmpty()) {
                registros.add(nuevo(alert.getId(), contacto.getId(), CANAL_CORREO, correo));
                if (identityApi.findUserByEmail(correo).isPresent()) {
                    registros.add(nuevo(alert.getId(), contacto.getId(), CANAL_PUSH, "App Alerta Mujer"));
                }
            }
        }
        alertContactRepository.saveAll(registros);
        // Los avisos salen después de confirmar la transacción (módulo de notificaciones)
        events.publishEvent(new AlertActivatedEvent(alert.getId(), alert.getUserProfileId(), alert.getStartedAt()));
    }

    /** Contactos avisados en una alerta, con sus canales. Un envío abierto de una alerta terminada se informa como "expired". */
    public List<ContactoNotificado> contactosDeAlerta(Alert alert) {
        boolean terminada = alert.getEndedAt() != null || alert.getCancelledAt() != null
                || (alert.getStatus() != null && ALERTA_TERMINADA.contains(alert.getStatus().toLowerCase()));

        Map<Long, EmergencyContact> contactos = new LinkedHashMap<>();
        emergencyContactRepository.findByUserProfileId(alert.getUserProfileId())
                .forEach(c -> contactos.put(c.getId(), c));

        Map<Long, List<CanalEnvio>> canales = new LinkedHashMap<>();
        for (AlertContact registro : alertContactRepository.findByAlertId(alert.getId())) {
            String estado = registro.getStatus();
            if (terminada && ESTADOS_ABIERTOS.contains(estado)) {
                estado = "expired";
            }
            canales.computeIfAbsent(registro.getEmergencyContactId(), k -> new ArrayList<>())
                    .add(new CanalEnvio(registro.getChannel(), registro.getDestination(), estado,
                            registro.getSentAt(), registro.getDeliveredAt()));
        }

        List<ContactoNotificado> resultado = new ArrayList<>();
        canales.forEach((contactoId, lista) -> {
            EmergencyContact c = contactos.get(contactoId);
            resultado.add(new ContactoNotificado(contactoId,
                    c != null ? c.getContactName() : "Contacto",
                    c != null ? c.getRelationship() : null,
                    lista));
        });
        return resultado;
    }

    private AlertContact nuevo(Long alertId, Long contactoId, String canal, String destino) {
        AlertContact registro = new AlertContact();
        registro.setAlertId(alertId);
        registro.setEmergencyContactId(contactoId);
        registro.setChannel(canal);
        registro.setDestination(destino);
        return registro;
    }

    public record CanalEnvio(String channel, String destination, String status,
                             LocalDateTime sentAt, LocalDateTime deliveredAt) {}

    public record ContactoNotificado(Long contactId, String name, String relationship, List<CanalEnvio> channels) {}
}
