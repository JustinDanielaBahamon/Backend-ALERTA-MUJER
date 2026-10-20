package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.ReportesUsuariaUseCase;
import com.alertamujer.admin.application.port.out.ReporteUsuariaRepositorio;
import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.admin.domain.model.CambiosReporte;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Casos de moderación; la revisión se atribuye a la administradora de la sesión. */
@Service
public class ReportesUsuariaService implements ReportesUsuariaUseCase {

    private final ReporteUsuariaRepositorio repositorio;
    private final UsuariasPort usuarias;

    public ReportesUsuariaService(ReporteUsuariaRepositorio repositorio, UsuariasPort usuarias) {
        this.repositorio = repositorio;
        this.usuarias = usuarias;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReportView> listar(String estado) {
        Map<Long, UserSummary> nombres = usuarias.porId();
        List<UserReport> casos = estado == null ? repositorio.todos() : repositorio.conEstado(estado);
        return casos.stream()
                .sorted(Comparator.comparing(UserReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> vista(r, nombres.get(r.getReportedUserId()), nombres.get(r.getReporterUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserReportView obtener(Long id) {
        return vista(existente(id));
    }

    @Override
    @Transactional
    public UserReportView crear(String correoReportada, String motivo, String descripcion, String gravedad, Long adminId) {
        if (correoReportada == null || correoReportada.isBlank()) {
            throw new IllegalArgumentException("Indica el correo de la usuaria reportada");
        }
        UserSummary reportada = usuarias.conCorreo(correoReportada.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe una usuaria con ese correo"));
        return vista(repositorio.guardar(UserReport.reporte(reportada.id(), adminId, motivo, descripcion, gravedad)));
    }

    @Override
    @Transactional
    public UserReportView actualizar(Long id, CambiosReporte cambios, Long adminId) {
        UserReport r = existente(id);
        r.actualizar(cambios, adminId, LocalDateTime.now());
        return vista(repositorio.guardar(r));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorio.eliminar(existente(id).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public String evidencia(Long id) {
        return existente(id).getEvidence();
    }

    private UserReport existente(Long id) {
        return repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("UserReport", id));
    }

    private UserReportView vista(UserReport r) {
        return vista(r, usuarias.buscar(r.getReportedUserId()).orElse(null), usuarias.buscar(r.getReporterUserId()).orElse(null));
    }

    private static String nombre(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private static UserReportView vista(UserReport r, UserSummary reportada, UserSummary reporta) {
        return new UserReportView(r.getId(), r.getReportedUserId(), nombre(reportada),
                reportada != null ? reportada.email() : null, r.getReporterUserId(), nombre(reporta),
                r.getReason(), r.getDescription(), r.getSeverity(), r.getStatus(), r.getResolutionNotes(),
                r.getReviewedByAdminId(), r.getReviewedAt(), r.getCreatedAt(), r.getUpdatedAt(), r.tieneEvidencia());
    }
}
