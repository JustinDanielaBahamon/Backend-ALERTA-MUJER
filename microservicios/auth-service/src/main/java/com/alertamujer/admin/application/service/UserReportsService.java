package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.UserReportsUseCase;
import com.alertamujer.admin.application.port.out.UserReportRepository;
import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.admin.domain.model.ReportChanges;
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
public class UserReportsService implements UserReportsUseCase {

    private final UserReportRepository repository;
    private final UsersPort users;

    public UserReportsService(UserReportRepository repository, UsersPort users) {
        this.repository = repository;
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReportView> listAll(String status) {
        Map<Long, UserSummary> names = users.byId();
        List<UserReport> cases = status == null ? repository.all() : repository.withStatus(status);
        return cases.stream()
                .sorted(Comparator.comparing(UserReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> view(r, names.get(r.getReportedUserId()), names.get(r.getReporterUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserReportView get(Long id) {
        return view(existing(id));
    }

    @Override
    @Transactional
    public UserReportView create(String reportedEmail, String reason, String description, String severity, Long adminId) {
        if (reportedEmail == null || reportedEmail.isBlank()) {
            throw new IllegalArgumentException("Indica el correo de la usuaria reportada");
        }
        UserSummary reported = users.withEmail(reportedEmail.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe una usuaria con ese correo"));
        return view(repository.save(UserReport.userReport(reported.id(), adminId, reason, description, severity)));
    }

    @Override
    @Transactional
    public UserReportView update(Long id, ReportChanges changes, Long adminId) {
        UserReport r = existing(id);
        r.update(changes, adminId, LocalDateTime.now());
        return view(repository.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(existing(id).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public String evidence(Long id) {
        return existing(id).getEvidence();
    }

    private UserReport existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("UserReport", id));
    }

    private UserReportView view(UserReport r) {
        return view(r, users.find(r.getReportedUserId()).orElse(null), users.find(r.getReporterUserId()).orElse(null));
    }

    private static String name(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private static UserReportView view(UserReport r, UserSummary reported, UserSummary reporter) {
        return new UserReportView(r.getId(), r.getReportedUserId(), name(reported),
                reported != null ? reported.email() : null, r.getReporterUserId(), name(reporter),
                r.getReason(), r.getDescription(), r.getSeverity(), r.getStatus(), r.getResolutionNotes(),
                r.getReviewedByAdminId(), r.getReviewedAt(), r.getCreatedAt(), r.getUpdatedAt(), r.hasEvidence());
    }
}
