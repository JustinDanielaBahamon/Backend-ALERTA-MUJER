package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.InformesUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.admin.application.port.out.ReportExporterPort;
import com.alertamujer.admin.application.port.out.ReportExportRepository;
import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.admin.application.port.out.ZonesPort;
import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.domain.model.ReportExportChanges;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Informes de alertas: guardar, consultar con sus alertas y exportar. Solo conoce puertos. */
@Service
public class InformesService implements InformesUseCase {

    private final ReportExportRepository repository;
    private final AlertsPort alerts;
    private final UsersPort users;
    private final ZonesPort zones;
    private final ReportExporterPort exporter;

    public InformesService(ReportExportRepository repository, AlertsPort alerts, UsersPort users, ZonesPort zones,
                           ReportExporterPort exporter) {
        this.repository = repository;
        this.alerts = alerts;
        this.users = users;
        this.zones = zones;
        this.exporter = exporter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertReportView> listAll() {
        List<AdminAlertView> all = alerts.all();
        Map<Long, UserSummary> names = users.byId();
        return repository.all().stream()
                .sorted(Comparator.comparing(AlertReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> view(r, names.get(r.getCreatedByUserId()), alertsOf(r, all).size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertReportView get(Long id) {
        return view(existing(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportStats stats() {
        List<AlertReport> informes = repository.all();
        long exports = informes.stream().mapToLong(r -> r.getExportCount() == null ? 0 : r.getExportCount()).sum();
        return new ReportStats(informes.size(), alerts.all().size(), zones.active(), exports);
    }

    @Override
    @Transactional
    public AlertReportView create(ReportExportChanges changes, Long adminUserId) {
        return view(repository.save(AlertReport.created(changes, adminUserId)));
    }

    @Override
    @Transactional
    public AlertReportView update(Long id, ReportExportChanges changes) {
        AlertReport r = existing(id);
        r.apply(changes);
        r.setUpdatedAt(LocalDateTime.now());
        return view(repository.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(existing(id).getId());
    }

    @Override
    @Transactional
    public byte[] export(Long id, boolean pdf) {
        AlertReport r = existing(id);
        AlertReportView report = view(r);
        List<AdminAlertView> included = alertsOf(r, alerts.all());
        byte[] file = pdf ? exporter.pdf(report, included) : exporter.csv(report, included);
        r.recordExport();
        repository.save(r);
        return file;
    }

    /** Alertas que cubre el informe: las elegidas o, si no hay, las que cumplen sus filtros. */
    static List<AdminAlertView> alertsOf(AlertReport r, List<AdminAlertView> all) {
        return all.stream().filter(a -> r.includes(a.id(), a.startedAt(), a.status())).toList();
    }

    private AlertReport existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("AlertReport", id));
    }

    private AlertReportView view(AlertReport r) {
        UserSummary creator = r.getCreatedByUserId() == null ? null : users.find(r.getCreatedByUserId()).orElse(null);
        return view(r, creator, alertsOf(r, alerts.all()).size());
    }

    private static AlertReportView view(AlertReport r, UserSummary creator, int alertCount) {
        return new AlertReportView(r.getId(), r.getName(), r.getCity(), r.getStatus(), r.getUserLabel(), r.getEvent(),
                r.getDescription(), r.getFormat(), r.chosenIds().stream().sorted().toList(), r.getFromDate(), r.getToDate(),
                r.getAlertStatus(), alertCount, r.getExportCount(), creator == null ? null : creator.fullName(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
