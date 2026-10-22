package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ZoneReportsUseCase;
import com.alertamujer.zones.application.port.out.IdentityPort;
import com.alertamujer.zones.application.port.out.ZoneReportRepository;
import com.alertamujer.zones.application.port.out.ZoneRepository;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Zone reports: the author comes from the token (user_profile.id), never from the body. */
@Service
public class ZoneReportsService implements ZoneReportsUseCase {

    private final ZoneReportRepository repository;
    private final ZoneRepository zones;
    private final IdentityPort identity;

    public ZoneReportsService(ZoneReportRepository repository, ZoneRepository zones, IdentityPort identity) {
        this.repository = repository;
        this.zones = zones;
        this.identity = identity;
    }

    @Override
    public List<ZoneReport> all() {
        return repository.all();
    }

    @Override
    public Optional<ZoneReport> one(Long id, Long userId) {
        Long profile = identity.profileOf(userId).orElse(null);
        return repository.find(id).filter(r -> r.esDe(profile));
    }

    @Override
    public List<ZoneReport> ofZone(Long zoneId) {
        return repository.ofZone(zoneId);
    }

    @Override
    public List<ZoneReport> ofUser(Long requestedId, Long userId, boolean admin) {
        Long own = identity.requiredProfile(userId);
        if (requestedId.equals(userId) || requestedId.equals(own)) return repository.ofProfile(own);
        if (!admin) throw new AccessDeniedException("No puedes ver los reportes de otra usuaria");
        return repository.ofProfile(identity.profileOf(requestedId).orElse(requestedId));
    }

    @Override
    public List<ZoneReport> withStatus(String status) {
        return repository.withStatus(status);
    }

    @Override
    @Transactional
    public ZoneReport create(Long zoneId, String classification, String comment, Long userId) {
        if (zoneId == null || zones.find(zoneId).isEmpty()) throw new ResourceNotFoundException("Zone", zoneId);
        return repository.save(ZoneReport.created(identity.requiredProfile(userId), zoneId, classification, comment));
    }

    @Override
    @Transactional
    public ZoneReport approve(Long id, Integer adminId) {
        ZoneReport r = existing(id);
        r.approve(adminId, LocalDateTime.now());
        return repository.save(r);
    }

    @Override
    @Transactional
    public ZoneReport reject(Long id, Integer adminId) {
        ZoneReport r = existing(id);
        r.reject(adminId, LocalDateTime.now());
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        ZoneReport r = existing(id);
        if (!r.esDe(identity.requiredProfile(userId))) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        repository.delete(id);
    }

    private ZoneReport existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
    }
}
