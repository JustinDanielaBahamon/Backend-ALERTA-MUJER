package com.alertamujer.alerts.application;

import com.alertamujer.alerts.domain.model.FrequentLocation;
import com.alertamujer.alerts.infrastructure.repository.FrequentLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FrequentLocationService {

    private final FrequentLocationRepository frequentLocationRepository;

    public FrequentLocationService(FrequentLocationRepository frequentLocationRepository) {
        this.frequentLocationRepository = frequentLocationRepository;
    }

    public List<FrequentLocation> getAllByUserProfileId(Long userProfileId) {
        if (userProfileId == null) {
            return frequentLocationRepository.findAll();
        }
        return frequentLocationRepository.findByUserProfileId(userProfileId);
    }

    public List<FrequentLocation> getActiveByUserProfileId(Long userProfileId) {
        return frequentLocationRepository.findByUserProfileIdAndIsActiveTrue(userProfileId);
    }

    public FrequentLocation getById(Long id) {
        return frequentLocationRepository.findById(id).orElse(null);
    }

    @Transactional
    public FrequentLocation create(FrequentLocation frequentLocation) {
        frequentLocation.setCreatedAt(java.time.LocalDateTime.now());
        if (frequentLocation.getRiskLevel() == null || frequentLocation.getRiskLevel().isBlank()) {
            frequentLocation.setRiskLevel("moderada");
        }
        return frequentLocationRepository.save(frequentLocation);
    }

    @Transactional
    public FrequentLocation update(Long id, FrequentLocation frequentLocation) {
        FrequentLocation existing = frequentLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FrequentLocation not found"));
        
        existing.setName(frequentLocation.getName() != null && !frequentLocation.getName().isBlank()
                ? frequentLocation.getName()
                : existing.getName());
        existing.setAddress(frequentLocation.getAddress());
        existing.setCity(frequentLocation.getCity());
        existing.setLatitude(frequentLocation.getLatitude() != null ? frequentLocation.getLatitude() : existing.getLatitude());
        existing.setLongitude(frequentLocation.getLongitude() != null ? frequentLocation.getLongitude() : existing.getLongitude());
        existing.setNotes(frequentLocation.getNotes());
        existing.setRiskLevel(frequentLocation.getRiskLevel() != null && !frequentLocation.getRiskLevel().isBlank()
                ? frequentLocation.getRiskLevel()
                : existing.getRiskLevel());
        existing.setIsActive(frequentLocation.getIsActive() != null ? frequentLocation.getIsActive() : existing.getIsActive());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return frequentLocationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        frequentLocationRepository.deleteById(id);
    }
}
