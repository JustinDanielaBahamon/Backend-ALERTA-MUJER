package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ReportesZonaUseCase;
import com.alertamujer.zones.application.port.out.IdentityPort;
import com.alertamujer.zones.application.port.out.ReporteZonaRepositorio;
import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Reportes de zonas: la autora sale del token (user_profile.id), nunca del body. */
@Service
public class ReportesZonaService implements ReportesZonaUseCase {

    private final ReporteZonaRepositorio repository;
    private final ZonaRepositorio zones;
    private final IdentityPort identity;

    public ReportesZonaService(ReporteZonaRepositorio repository, ZonaRepositorio zones, IdentityPort identity) {
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
    public List<ZoneReport> deZona(Long zoneId) {
        return repository.deZona(zoneId);
    }

    @Override
    public List<ZoneReport> ofUser(Long idPedido, Long userId, boolean admin) {
        Long own = identity.perfilRequerido(userId);
        if (idPedido.equals(userId) || idPedido.equals(own)) return repository.dePerfil(own);
        if (!admin) throw new AccessDeniedException("No puedes ver los reportes de otra usuaria");
        return repository.dePerfil(identity.profileOf(idPedido).orElse(idPedido));
    }

    @Override
    public List<ZoneReport> withStatus(String status) {
        return repository.withStatus(status);
    }

    @Override
    @Transactional
    public ZoneReport create(Long zoneId, String clasificacion, String comentario, Long userId) {
        if (zoneId == null || zones.find(zoneId).isEmpty()) throw new ResourceNotFoundException("Zone", zoneId);
        return repository.save(ZoneReport.created(identity.perfilRequerido(userId), zoneId, clasificacion, comentario));
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
        if (!r.esDe(identity.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        repository.delete(id);
    }

    private ZoneReport existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
    }
}
