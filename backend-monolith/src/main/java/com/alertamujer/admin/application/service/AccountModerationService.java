package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.admin.infrastructure.repository.AccountDeletionRequestRepository;
import com.alertamujer.admin.infrastructure.repository.ModerationActionRepository;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.FotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bloqueos y eliminaciones de cuentas desde el panel.
 *
 * Bloquear se hace en Gestión de usuarios y exige un motivo (y opcionalmente una captura): queda
 * como un caso cerrado en Moderación, donde se ve quién bloqueó y por qué. Eliminar una cuenta es
 * irreversible, así que una administradora lo solicita y otra distinta lo aprueba.
 */
@Service
public class AccountModerationService {

    private static final int MIN_MOTIVO = 10;

    private final UserReportRepository reportes;
    private final ModerationActionRepository acciones;
    private final AccountDeletionRequestRepository solicitudes;
    private final IdentityAdminApi identityAdminApi;
    private final IdentityApi identityApi;

    public AccountModerationService(UserReportRepository reportes, ModerationActionRepository acciones,
                                    AccountDeletionRequestRepository solicitudes, IdentityAdminApi identityAdminApi,
                                    IdentityApi identityApi) {
        this.reportes = reportes;
        this.acciones = acciones;
        this.solicitudes = solicitudes;
        this.identityAdminApi = identityAdminApi;
        this.identityApi = identityApi;
    }

    // ── Bloqueo ────────────────────────────────────────────────────

    @Transactional
    public void bloquear(Long userId, BloqueoRequest req, Long adminId) {
        String motivo = requerido(req == null ? null : req.reason(), "Elige el motivo del bloqueo", 100);
        String detalle = texto(req.description(), "Explica por qué se bloquea la cuenta (mínimo " + MIN_MOTIVO + " caracteres)");
        String evidencia = FotoDataUri.validar(req.evidence());
        existente(userId);
        // updateUserAccount impide que una administradora se bloquee a sí misma
        identityAdminApi.updateUserAccount(userId, new UserAccountChanges("blocked", null, null, null, null), adminId);

        UserReport caso = new UserReport();
        caso.setReportedUserId(userId);
        caso.setReporterUserId(adminId);
        caso.setReason(motivo);
        caso.setDescription(detalle);
        caso.setSeverity("high");
        caso.setStatus("resolved");
        caso.setResolutionNotes("Cuenta bloqueada desde Gestión de usuarios");
        caso.setReviewedByAdminId(adminId.intValue());
        caso.setReviewedAt(LocalDateTime.now());
        caso.setEvidence(evidencia);
        caso = reportes.save(caso);
        registrar(caso.getId(), adminId, "block", detalle);
    }

    @Transactional
    public void desbloquear(Long userId, String nota, Long adminId) {
        String detalle = texto(nota, "Explica por qué se desbloquea la cuenta (mínimo " + MIN_MOTIVO + " caracteres)");
        existente(userId);
        identityAdminApi.updateUserAccount(userId, new UserAccountChanges("active", null, null, null, null), adminId);
        // Se anota en el último caso de la usuaria; si no tiene (bloqueo anterior a este registro), se crea uno
        UserReport caso = reportes.findByReportedUserId(userId).stream()
                .max(Comparator.comparing(UserReport::getCreatedAt))
                .orElseGet(() -> {
                    UserReport nuevo = new UserReport();
                    nuevo.setReportedUserId(userId);
                    nuevo.setReporterUserId(adminId);
                    nuevo.setReason("Desbloqueo");
                    nuevo.setDescription(detalle);
                    nuevo.setStatus("resolved");
                    nuevo.setReviewedByAdminId(adminId.intValue());
                    nuevo.setReviewedAt(LocalDateTime.now());
                    return reportes.save(nuevo);
                });
        registrar(caso.getId(), adminId, "unblock", detalle);
    }

    /** Lo que se ha hecho con la cuenta (bloqueos, desbloqueos, eliminación), del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<HistorialItem> historial(Long userId) {
        Map<Long, UserReport> casos = reportes.findByReportedUserId(userId).stream()
                .collect(Collectors.toMap(UserReport::getId, r -> r));
        Map<Long, UserSummary> usuarios = identityApi.usersById();
        Stream<HistorialItem> deAcciones = casos.isEmpty() ? Stream.empty()
                : acciones.findByUserReportIdIn(casos.keySet()).stream().map(a -> {
                    UserReport caso = casos.get(a.getUserReportId());
                    return new HistorialItem(a.getCreatedAt(), a.getActionType(), nombre(usuarios.get(a.getAdminPerfilId().longValue())),
                            "block".equals(a.getActionType()) ? caso.getReason() : null, a.getNotes(),
                            caso.getId(), caso.getEvidence() != null);
                });
        Stream<HistorialItem> deSolicitudes = solicitudes.findAllByOrderByCreatedAtDesc().stream()
                .filter(s -> s.getUserId().equals(userId))
                .map(s -> new HistorialItem(s.getCreatedAt(), "deletion_" + s.getStatus(), nombre(usuarios.get(s.getRequestedBy())),
                        null, s.getReason(), null, false));
        return Stream.concat(deAcciones, deSolicitudes)
                .sorted(Comparator.comparing(HistorialItem::fecha).reversed())
                .toList();
    }

    // ── Eliminación con dos administradoras ────────────────────────

    @Transactional
    public SolicitudView solicitarEliminacion(Long userId, String motivo, Long adminId) {
        String detalle = texto(motivo, "Explica por qué se elimina la cuenta (mínimo " + MIN_MOTIVO + " caracteres)");
        existente(userId);
        if (userId.equals(adminId)) throw new IllegalArgumentException("No puedes eliminar tu propia cuenta");
        if (solicitudes.existsByUserIdAndStatus(userId, "pending")) {
            throw new IllegalArgumentException("Ya hay una solicitud pendiente para esta cuenta");
        }
        AccountDeletionRequest s = new AccountDeletionRequest();
        s.setUserId(userId);
        s.setRequestedBy(adminId);
        s.setReason(detalle);
        return vista(solicitudes.save(s), identityApi.usersById());
    }

    @Transactional(readOnly = true)
    public List<SolicitudView> solicitudes() {
        Map<Long, UserSummary> usuarios = identityApi.usersById();
        return solicitudes.findAllByOrderByCreatedAtDesc().stream().map(s -> vista(s, usuarios)).toList();
    }

    @Transactional
    public SolicitudView aprobar(Long id, String nota, Long adminId) {
        AccountDeletionRequest s = pendiente(id);
        if (s.getRequestedBy().equals(adminId)) {
            throw new IllegalArgumentException("La aprobación debe hacerla otra administradora");
        }
        identityAdminApi.anonymizeAccount(s.getUserId());
        return decidir(s, "approved", nota, adminId);
    }

    /** Rechazar o, si es quien la pidió, cancelar la solicitud. */
    @Transactional
    public SolicitudView rechazar(Long id, String nota, Long adminId) {
        return decidir(pendiente(id), "rejected", nota, adminId);
    }

    // ── Apoyo ──────────────────────────────────────────────────────

    private SolicitudView decidir(AccountDeletionRequest s, String estado, String nota, Long adminId) {
        s.setStatus(estado);
        s.setDecidedBy(adminId);
        s.setDecisionNote(nota == null || nota.isBlank() ? null : recortar(nota.trim(), 500));
        s.setDecidedAt(LocalDateTime.now());
        return vista(solicitudes.save(s), identityApi.usersById());
    }

    private AccountDeletionRequest pendiente(Long id) {
        AccountDeletionRequest s = solicitudes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountDeletionRequest", id));
        if (!"pending".equals(s.getStatus())) throw new IllegalArgumentException("Esta solicitud ya fue decidida");
        return s;
    }

    private void existente(Long userId) {
        identityApi.findUser(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void registrar(Long casoId, Long adminId, String tipo, String nota) {
        ModerationAction accion = new ModerationAction();
        accion.setUserReportId(casoId);
        accion.setAdminPerfilId(adminId.intValue());
        accion.setActionType(tipo);
        accion.setNotes(nota);
        acciones.save(accion);
    }

    private static String texto(String valor, String mensaje) {
        String v = valor == null ? "" : valor.trim();
        if (v.length() < MIN_MOTIVO) throw new IllegalArgumentException(mensaje);
        return recortar(v, 1000);
    }

    private static String requerido(String valor, String mensaje, int max) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensaje);
        return recortar(valor.trim(), max);
    }

    private static String recortar(String v, int max) {
        return v.length() > max ? v.substring(0, max) : v;
    }

    private static String nombre(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private static SolicitudView vista(AccountDeletionRequest s, Map<Long, UserSummary> usuarios) {
        UserSummary cuenta = usuarios.get(s.getUserId());
        return new SolicitudView(s.getId(), s.getUserId(), nombre(cuenta), cuenta != null ? cuenta.email() : null,
                s.getRequestedBy(), nombre(usuarios.get(s.getRequestedBy())), s.getReason(), s.getStatus(),
                s.getDecidedBy(), s.getDecidedBy() == null ? null : nombre(usuarios.get(s.getDecidedBy())),
                s.getDecisionNote(), s.getCreatedAt(), s.getDecidedAt());
    }

    public record BloqueoRequest(String reason, String description, String evidence) {}

    public record HistorialItem(LocalDateTime fecha, String tipo, String adminNombre, String motivo, String nota,
                                Long casoId, boolean tieneEvidencia) {}

    public record SolicitudView(Long id, Long userId, String userName, String userEmail, Long requestedBy,
                                String requestedByName, String reason, String status, Long decidedBy,
                                String decidedByName, String decisionNote, LocalDateTime createdAt,
                                LocalDateTime decidedAt) {}
}
