package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ZonesUseCase;
import com.alertamujer.zones.application.port.out.ZoneRepository;
import com.alertamujer.zones.domain.model.ZoneData;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Map zones. Only knows the zones port. */
@Service
public class ZonesService implements ZonesUseCase {

    private final ZoneRepository repository;

    public ZonesService(ZoneRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Zone> all() {
        return repository.all();
    }

    @Override
    public Optional<Zone> one(Long id) {
        return repository.find(id);
    }

    @Override
    public List<Zone> ofCity(String city) {
        return repository.ofCity(city);
    }

    @Override
    public List<Zone> ofType(String type) {
        return repository.ofType(type);
    }

    @Override
    @Transactional
    public Zone create(ZoneData data) {
        return repository.save(Zone.newOne(data));
    }

    @Override
    @Transactional
    public Zone replace(Long id, ZoneData data) {
        Zone zone = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Zone", id));
        zone.replace(data);
        return repository.save(zone);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return repository.all().stream().filter(Zone::active).count();
    }
}
