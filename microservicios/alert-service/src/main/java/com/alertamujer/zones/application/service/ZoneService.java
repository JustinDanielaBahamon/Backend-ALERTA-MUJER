package com.alertamujer.zones.application.service;

import com.alertamujer.zones.domain.model.Zone;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Optional<Zone> getZoneById(Long id) {
        return zoneRepository.findById(id);
    }

    public List<Zone> getZonesByCity(String city) {
        return zoneRepository.findByCity(city);
    }

    public List<Zone> getZonesByType(String type) {
        return zoneRepository.findByZoneType(type);
    }

    public List<Zone> getActiveZones() {
        return zoneRepository.findByIsActiveTrue();
    }

    public Zone createZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    public Zone updateZone(Long id, Zone zoneDetails) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        
        zone.setName(zoneDetails.getName());
        zone.setZoneType(zoneDetails.getZoneType());
        zone.setRiskLevel(zoneDetails.getRiskLevel());
        zone.setDescription(zoneDetails.getDescription());
        zone.setAddress(zoneDetails.getAddress());
        zone.setCity(zoneDetails.getCity());
        zone.setLatitude(zoneDetails.getLatitude());
        zone.setLongitude(zoneDetails.getLongitude());
        zone.setRadiusMeters(zoneDetails.getRadiusMeters());
        zone.setIsActive(zoneDetails.getIsActive());
        
        return zoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }
}
