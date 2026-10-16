package com.alertamujer.zones.application.service;

import com.alertamujer.zones.api.ZonesApi;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ZonesApiService implements ZonesApi {

    private final ZoneRepository zoneRepository;

    public ZonesApiService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @Override
    public long countActiveZones() {
        return zoneRepository.findAll().stream().filter(z -> Boolean.TRUE.equals(z.getIsActive())).count();
    }
}
