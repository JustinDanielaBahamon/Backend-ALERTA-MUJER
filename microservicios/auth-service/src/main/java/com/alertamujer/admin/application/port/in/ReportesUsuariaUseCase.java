package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.CambiosReporte;

import java.time.LocalDateTime;
import java.util.List;

/** Casos de moderación, con el nombre y correo de la usuaria reportada y de quien reporta. */
public interface ReportesUsuariaUseCase {

    /** Todos (estado null) o los de un estado, del más reciente al más antiguo. */
    List<UserReportView> listar(String estado);

    UserReportView obtener(Long id);

    /** Reporte de la administración sobre una usuaria identificada por su correo. */
    UserReportView crear(String correoReportada, String motivo, String descripcion, String gravedad, Long adminId);

    UserReportView actualizar(Long id, CambiosReporte cambios, Long adminId);

    void eliminar(Long id);

    /** Captura adjunta al caso (data URI) o null. */
    String evidencia(Long id);

    record UserReportView(Long id, Long reportedUserId, String reportedUserName, String reportedUserEmail,
                          Long reporterUserId, String reporterName, String reason, String description,
                          String severity, String status, String resolutionNotes, Integer reviewedByAdminId,
                          LocalDateTime reviewedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                          boolean hasEvidence) {}
}
