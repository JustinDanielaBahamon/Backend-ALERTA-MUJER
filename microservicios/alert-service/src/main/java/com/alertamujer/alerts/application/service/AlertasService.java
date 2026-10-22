package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ConsultarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.GestionarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.CambiosAlerta;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Casos de uso de las alertas. Solo conoce puertos, no la base ni los otros microservicios. */
@Service
public class AlertasService implements GestionarAlertasUseCase, ConsultarAlertasUseCase {

    private final AlertaRepositorio repository;
    private final DestinatariosService destinatarios;
    private final IdentityPort identidad;
    private final EventosAlertaPort events;

    public AlertasService(AlertaRepositorio repository, DestinatariosService destinatarios,
                          IdentityPort identidad, EventosAlertaPort events) {
        this.repository = repository;
        this.destinatarios = destinatarios;
        this.identidad = identidad;
        this.events = events;
    }

    // ── De la usuaria ───────────────────────────────────────────────

    @Override
    public List<Alert> mias(Long userId) {
        return repository.ofUser(profileOf(userId)).stream()
                .sorted(Comparator.comparing(Alert::getStartedAt).reversed())
                .toList();
    }

    @Override
    public Optional<Alert> una(Long id, Long userId) {
        Long perfil = identidad.profileOf(userId).orElse(null);
        if (perfil == null) return Optional.empty();
        return repository.find(id).filter(a -> a.esDe(perfil));
    }

    @Override
    public List<Alert> deUsuaria(Long idPedido, Long userId) {
        Long propio = profileOf(userId);
        UserSummary actual = identidad.usuaria(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        // users.id y user_profile.id no coinciden necesariamente (en producción son distintos)
        if (idPedido.equals(userId) || idPedido.equals(propio)) {
            return repository.ofUser(propio);
        }
        if (!actual.admin()) {
            throw new AccessDeniedException("No puedes ver alertas de otro usuario");
        }
        // Para la administración el id se interpreta como users.id y, si no existe, como user_profile.id
        return repository.ofUser(identidad.profileOf(idPedido).orElse(idPedido));
    }

    @Override
    @Transactional
    public Alert activar(NuevaAlerta nueva, Long userId) {
        // La dueña sale del JWT; nunca se confía en un perfil que llegue en el body (evita IDOR)
        Long perfil = profileOf(userId);
        Optional<Alert> enCurso = repository.enCurso(perfil);
        if (enCurso.isPresent()) return enCurso.get();

        Alert alert = new Alert();
        alert.setUserProfileId(perfil);
        alert.setDeviceId(nueva.deviceId());
        if (nueva.alertType() != null) alert.setAlertType(nueva.alertType());
        alert.setActivationMethod(nueva.activationMethod());
        if (nueva.status() != null) alert.setStatus(nueva.status());
        alert.setMessage(nueva.message());
        alert.setLatitude(nueva.latitude());
        alert.setLongitude(nueva.longitude());
        if (nueva.startedAt() != null) alert.setStartedAt(nueva.startedAt());

        Alert creada = repository.save(alert);
        destinatarios.registrar(creada, nueva.contactIds());
        return creada;
    }

    @Override
    @Transactional
    public Alert actualizar(Long id, CambiosAlerta cambios, Long userId) {
        Alert alert = propia(id, userId, "No puedes modificar alertas de otro usuario");
        LocalDateTime ahora = LocalDateTime.now();
        // Al terminar una alerta activa se avisa a los contactos que la recibieron
        if (alert.actualizar(cambios, ahora)) events.alertaTerminada(alert, ahora);
        return repository.save(alert);
    }

    @Override
    @Transactional
    public Alert conservarEvidencias(Long id, boolean conservar, Long userId) {
        Alert alert = propia(id, userId, "No puedes modificar alertas de otro usuario");
        alert.conservarEvidencias(conservar, repository.kept(alert.getUserProfileId()));
        return repository.save(alert);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        propia(id, userId, "No puedes eliminar alertas de otro usuario");
        repository.delete(id);
    }

    // ── Para otros módulos ──────────────────────────────────────────

    @Override
    public Optional<Alert> find(Long id) {
        return id == null ? Optional.empty() : repository.find(id);
    }

    @Override
    public List<Alert> findMany(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : repository.findMany(ids);
    }

    @Override
    public List<Alert> todas() {
        return repository.todas();
    }

    @Override
    public Map<Long, Long> conteoPorPerfil() {
        return repository.todas().stream()
                .filter(a -> a.getUserProfileId() != null)
                .collect(Collectors.groupingBy(Alert::getUserProfileId, Collectors.counting()));
    }

    @Override
    public long cuantasActivas() {
        return repository.activas().size();
    }

    @Override
    public List<Long> idsDelPerfil(Long perfil) {
        return repository.ofUser(perfil).stream().map(Alert::getId).toList();
    }

    @Override
    public Set<Long> keepEvidence(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        return repository.findMany(ids).stream()
                .filter(Alert::isKeepEvidence)
                .map(Alert::getId)
                .collect(Collectors.toSet());
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Long profileOf(Long userId) {
        return identidad.profileOf(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private Alert propia(Long id, Long userId, String mensajeAjena) {
        Alert alert = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        if (!alert.esDe(profileOf(userId))) throw new AccessDeniedException(mensajeAjena);
        return alert;
    }
}
