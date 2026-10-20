package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.DestinatarioRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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
public class DestinatariosService implements DestinatariosAlertaUseCase {

    private final DestinatarioRepositorio repositorio;
    private final ContactoRepositorio contactos;
    private final IdentidadPort identidad;
    private final EventosAlertaPort eventos;

    public DestinatariosService(DestinatarioRepositorio repositorio, ContactoRepositorio contactos,
                                IdentidadPort identidad, EventosAlertaPort eventos) {
        this.repositorio = repositorio;
        this.contactos = contactos;
        this.identidad = identidad;
        this.eventos = eventos;
    }

    /**
     * Crea un registro por cada contacto avisado y canal disponible. Sin contactIds se avisa a todos
     * los contactos de la usuaria; con contactIds, solo a esos (deben ser suyos: un id ajeno o borrado
     * cancela la alerta en vez de avisar a otras personas).
     */
    @Transactional
    public void registrar(Alert alerta, List<Long> contactIds) {
        List<EmergencyContact> avisados = contactos.deLaUsuaria(alerta.getUserProfileId());
        if (contactIds != null && !contactIds.isEmpty()) {
            Set<Long> elegidos = contactIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            avisados = avisados.stream().filter(c -> elegidos.contains(c.getId())).toList();
            if (avisados.size() != elegidos.size()) {
                throw new IllegalArgumentException("Alguno de los contactos elegidos no existe");
            }
        }
        List<AlertContact> registros = new ArrayList<>();
        for (EmergencyContact contacto : avisados) {
            String correo = contacto.correoParaAvisar();
            if (correo.isEmpty()) continue;
            registros.add(AlertContact.nuevo(alerta.getId(), contacto.getId(), AlertContact.CANAL_CORREO, correo));
            if (identidad.tieneCuenta(correo)) {
                registros.add(AlertContact.nuevo(alerta.getId(), contacto.getId(), AlertContact.CANAL_PUSH, "App Alerta Mujer"));
            }
        }
        repositorio.guardarTodos(registros);
        // Los avisos salen después de confirmar la transacción (módulo de notificaciones)
        eventos.alertaActivada(alerta);
    }

    @Override
    public List<ContactoNotificado> contactosDe(Alert alerta) {
        boolean terminada = alerta.terminada();
        Map<Long, EmergencyContact> porId = new LinkedHashMap<>();
        contactos.deLaUsuaria(alerta.getUserProfileId()).forEach(c -> porId.put(c.getId(), c));

        Map<Long, List<CanalEnvio>> canales = new LinkedHashMap<>();
        for (AlertContact d : repositorio.deLaAlerta(alerta.getId())) {
            canales.computeIfAbsent(d.getEmergencyContactId(), k -> new ArrayList<>())
                    .add(new CanalEnvio(d.getChannel(), d.getDestination(), d.estadoVisible(terminada),
                            d.getSentAt(), d.getDeliveredAt()));
        }
        List<ContactoNotificado> resultado = new ArrayList<>();
        canales.forEach((contactoId, lista) -> {
            EmergencyContact c = porId.get(contactoId);
            resultado.add(new ContactoNotificado(contactoId,
                    c != null ? c.getContactName() : contactoId == null ? "Contacto eliminado" : "Contacto",
                    c != null ? c.getRelationship() : null,
                    lista));
        });
        return resultado;
    }

    @Override
    public List<AlertRecipient> destinatarios(Long alertId) {
        List<AlertContact> envios = repositorio.deLaAlerta(alertId);
        Map<Long, String> nombres = contactos.buscarVarios(envios.stream()
                        .map(AlertContact::getEmergencyContactId).filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(EmergencyContact::getId,
                        c -> c.getContactName() == null ? "" : c.getContactName(), (a, b) -> a));
        return envios.stream()
                .map(e -> new AlertRecipient(e.getId(), e.getEmergencyContactId(), nombres.get(e.getEmergencyContactId()),
                        e.getChannel(), e.getDestination()))
                .toList();
    }

    @Override
    @Transactional
    public void registrarEntrega(Long destinatarioId, String estado, String error) {
        repositorio.buscar(destinatarioId).ifPresent(d -> {
            d.registrarIntento(estado, error, LocalDateTime.now());
            repositorio.guardar(d);
        });
    }

    @Override
    public List<Long> alertasAvisadasA(String correo) {
        if (correo == null || correo.isBlank()) return List.of();
        return repositorio.alertasAvisadasA(correo.trim());
    }
}
