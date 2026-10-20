package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ConsultarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.GestionarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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

    private final AlertaRepositorio repositorio;
    private final DestinatariosService destinatarios;
    private final IdentidadPort identidad;
    private final EventosAlertaPort eventos;

    public AlertasService(AlertaRepositorio repositorio, DestinatariosService destinatarios,
                          IdentidadPort identidad, EventosAlertaPort eventos) {
        this.repositorio = repositorio;
        this.destinatarios = destinatarios;
        this.identidad = identidad;
        this.eventos = eventos;
    }

    // ── De la usuaria ───────────────────────────────────────────────

    @Override
    public List<Alert> mias(Long userId) {
        return repositorio.deLaUsuaria(perfilDe(userId)).stream()
                .sorted(Comparator.comparing(Alert::getStartedAt).reversed())
                .toList();
    }

    @Override
    public Optional<Alert> una(Long id, Long userId) {
        Long perfil = identidad.perfilDe(userId).orElse(null);
        if (perfil == null) return Optional.empty();
        return repositorio.buscar(id).filter(a -> a.esDe(perfil));
    }

    @Override
    public List<Alert> deUsuaria(Long idPedido, Long userId) {
        Long propio = perfilDe(userId);
        UserSummary actual = identidad.usuaria(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        // users.id y user_profile.id no coinciden necesariamente (en producción son distintos)
        if (idPedido.equals(userId) || idPedido.equals(propio)) {
            return repositorio.deLaUsuaria(propio);
        }
        if (!actual.admin()) {
            throw new AccessDeniedException("No puedes ver alertas de otro usuario");
        }
        // Para la administración el id se interpreta como users.id y, si no existe, como user_profile.id
        return repositorio.deLaUsuaria(identidad.perfilDe(idPedido).orElse(idPedido));
    }

    @Override
    @Transactional
    public Alert activar(NuevaAlerta nueva, Long userId) {
        // La dueña sale del JWT; nunca se confía en un perfil que llegue en el body (evita IDOR)
        Long perfil = perfilDe(userId);
        Optional<Alert> enCurso = repositorio.enCurso(perfil);
        if (enCurso.isPresent()) return enCurso.get();

        Alert alerta = new Alert();
        alerta.setUserProfileId(perfil);
        alerta.setDeviceId(nueva.deviceId());
        if (nueva.alertType() != null) alerta.setAlertType(nueva.alertType());
        alerta.setActivationMethod(nueva.activationMethod());
        if (nueva.status() != null) alerta.setStatus(nueva.status());
        alerta.setMessage(nueva.message());
        alerta.setLatitude(nueva.latitude());
        alerta.setLongitude(nueva.longitude());
        if (nueva.startedAt() != null) alerta.setStartedAt(nueva.startedAt());

        Alert creada = repositorio.guardar(alerta);
        destinatarios.registrar(creada, nueva.contactIds());
        return creada;
    }

    @Override
    @Transactional
    public Alert actualizar(Long id, CambiosAlerta cambios, Long userId) {
        Alert alerta = propia(id, userId, "No puedes modificar alertas de otro usuario");
        LocalDateTime ahora = LocalDateTime.now();
        // Al terminar una alerta activa se avisa a los contactos que la recibieron
        if (alerta.actualizar(cambios, ahora)) eventos.alertaTerminada(alerta, ahora);
        return repositorio.guardar(alerta);
    }

    @Override
    @Transactional
    public Alert conservarEvidencias(Long id, boolean conservar, Long userId) {
        Alert alerta = propia(id, userId, "No puedes modificar alertas de otro usuario");
        alerta.conservarEvidencias(conservar, repositorio.conservadas(alerta.getUserProfileId()));
        return repositorio.guardar(alerta);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        propia(id, userId, "No puedes eliminar alertas de otro usuario");
        repositorio.eliminar(id);
    }

    // ── Para otros módulos ──────────────────────────────────────────

    @Override
    public Optional<Alert> buscar(Long id) {
        return id == null ? Optional.empty() : repositorio.buscar(id);
    }

    @Override
    public List<Alert> buscarVarias(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : repositorio.buscarVarias(ids);
    }

    @Override
    public List<Alert> todas() {
        return repositorio.todas();
    }

    @Override
    public Map<Long, Long> conteoPorPerfil() {
        return repositorio.todas().stream()
                .filter(a -> a.getUserProfileId() != null)
                .collect(Collectors.groupingBy(Alert::getUserProfileId, Collectors.counting()));
    }

    @Override
    public long cuantasActivas() {
        return repositorio.activas().size();
    }

    @Override
    public List<Long> idsDelPerfil(Long perfil) {
        return repositorio.deLaUsuaria(perfil).stream().map(Alert::getId).toList();
    }

    @Override
    public Set<Long> conservanEvidencias(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        return repositorio.buscarVarias(ids).stream()
                .filter(Alert::isKeepEvidence)
                .map(Alert::getId)
                .collect(Collectors.toSet());
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Long perfilDe(Long userId) {
        return identidad.perfilDe(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private Alert propia(Long id, Long userId, String mensajeAjena) {
        Alert alerta = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        if (!alerta.esDe(perfilDe(userId))) throw new AccessDeniedException(mensajeAjena);
        return alerta;
    }
}
