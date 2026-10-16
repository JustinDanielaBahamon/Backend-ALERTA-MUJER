package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reportes de usuarias para la moderación. La vista agrega nombre y correo de la usuaria
 * reportada y de quien reporta; la revisión se atribuye a la administradora de la sesión.
 */
@Service
public class UserReportService {

    private static final Set<String> ESTADOS = Set.of("pending", "in_review", "reviewed", "resolved", "approved", "rejected");
    private static final Set<String> CERRADOS = Set.of("reviewed", "resolved", "approved", "rejected");
    private static final Set<String> GRAVEDADES = Set.of("low", "medium", "high", "critical");

    private final UserReportRepository userReportRepository;
    private final IdentityApi identityApi;

    public UserReportService(UserReportRepository userReportRepository, IdentityApi identityApi) {
        this.userReportRepository = userReportRepository;
        this.identityApi = identityApi;
    }

    @Transactional(readOnly = true)
    public List<UserReportView> listar(String estado) {
        Map<Long, UserSummary> usuarios = identityApi.usersById();
        List<UserReport> reportes = estado == null ? userReportRepository.findAll() : userReportRepository.findByStatus(estado);
        return reportes.stream()
                .sorted(Comparator.comparing(UserReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> vista(r, usuarios.get(r.getReportedUserId()), usuarios.get(r.getReporterUserId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserReportView obtener(Long id) {
        return vista(buscar(id));
    }

    /** Reporte creado por la administración sobre una usuaria identificada por su correo. */
    @Transactional
    public UserReportView crear(UserReportRequest req, Long adminUserId) {
        if (req.reportedUserEmail() == null || req.reportedUserEmail().isBlank()) {
            throw new IllegalArgumentException("Indica el correo de la usuaria reportada");
        }
        UserSummary reportada = identityApi.findUserByEmail(req.reportedUserEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe una usuaria con ese correo"));
        if (req.reason() == null || req.reason().isBlank() || req.description() == null || req.description().isBlank()) {
            throw new IllegalArgumentException("El motivo y la descripción son obligatorios");
        }
        UserReport r = new UserReport();
        r.setReportedUserId(reportada.id());
        r.setReporterUserId(adminUserId);
        r.setReason(req.reason().trim());
        r.setDescription(req.description().trim());
        r.setSeverity(validarGravedad(req.severity() == null ? "medium" : req.severity()));
        r.setStatus("pending");
        return vista(userReportRepository.save(r));
    }

    /** Cambia los campos enviados; al cerrar el reporte guarda quién y cuándo lo revisó. */
    @Transactional
    public UserReportView actualizar(Long id, UserReportRequest req, Long adminUserId) {
        UserReport r = buscar(id);
        if (req.reason() != null && !req.reason().isBlank()) r.setReason(req.reason().trim());
        if (req.description() != null && !req.description().isBlank()) r.setDescription(req.description().trim());
        if (req.severity() != null) r.setSeverity(validarGravedad(req.severity()));
        if (req.resolutionNotes() != null) r.setResolutionNotes(req.resolutionNotes().trim());
        if (req.status() != null) {
            if (!ESTADOS.contains(req.status())) throw new IllegalArgumentException("Estado no válido: " + req.status());
            r.setStatus(req.status());
            if (CERRADOS.contains(req.status())) {
                r.setReviewedAt(LocalDateTime.now());
                r.setReviewedByAdminId(adminUserId == null ? null : adminUserId.intValue());
            }
        }
        r.setUpdatedAt(LocalDateTime.now());
        return vista(userReportRepository.save(r));
    }

    @Transactional
    public void eliminar(Long id) {
        userReportRepository.delete(buscar(id));
    }

    private UserReport buscar(Long id) {
        return userReportRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("UserReport", id));
    }

    private String validarGravedad(String gravedad) {
        if (!GRAVEDADES.contains(gravedad)) throw new IllegalArgumentException("Gravedad no válida: " + gravedad);
        return gravedad;
    }

    private UserReportView vista(UserReport r) {
        return vista(r, identityApi.findUser(r.getReportedUserId()).orElse(null),
                identityApi.findUser(r.getReporterUserId()).orElse(null));
    }

    private static String nombre(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private UserReportView vista(UserReport r, UserSummary reportada, UserSummary reporta) {
        return new UserReportView(r.getId(), r.getReportedUserId(), nombre(reportada),
                reportada != null ? reportada.email() : null, r.getReporterUserId(), nombre(reporta),
                r.getReason(), r.getDescription(), r.getSeverity(), r.getStatus(), r.getResolutionNotes(),
                r.getReviewedByAdminId(), r.getReviewedAt(), r.getCreatedAt(), r.getUpdatedAt());
    }

    public record UserReportRequest(String reportedUserEmail, String reason, String description,
                                    String severity, String status, String resolutionNotes) {}

    public record UserReportView(Long id, Long reportedUserId, String reportedUserName, String reportedUserEmail,
                                 Long reporterUserId, String reporterName, String reason, String description,
                                 String severity, String status, String resolutionNotes, Integer reviewedByAdminId,
                                 LocalDateTime reviewedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
