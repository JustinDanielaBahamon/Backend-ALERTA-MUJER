package com.alertamujer.alerts.application;

import com.alertamujer.alerts.domain.model.FrequentLocation;
import com.alertamujer.alerts.infrastructure.repository.FrequentLocationRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FrequentLocationService {

    private final FrequentLocationRepository frequentLocationRepository;
    private final UserProfileRepository userProfileRepository;

    public FrequentLocationService(FrequentLocationRepository frequentLocationRepository, UserProfileRepository userProfileRepository) {
        this.frequentLocationRepository = frequentLocationRepository;
        this.userProfileRepository = userProfileRepository;
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

    public List<FrequentLocation> getByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));
        return frequentLocationRepository.findByUserProfileId(userProfile.getId());
    }

    public FrequentLocation getById(Long id) {
        return frequentLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FrequentLocation", id));
    }

    public FrequentLocation getById(Long id, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        FrequentLocation location = getById(id);
        if (!location.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes acceder a ubicaciones frecuentes de otro usuario");
        }
        return location;
    }

    @Transactional
    public FrequentLocation create(FrequentLocation frequentLocation, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        // Ignorar userProfileId del payload y usar el del JWT
        frequentLocation.setUserProfileId(userProfile.getId());
        frequentLocation.setCreatedAt(java.time.LocalDateTime.now());
        if (frequentLocation.getRiskLevel() == null || frequentLocation.getRiskLevel().isBlank()) {
            frequentLocation.setRiskLevel("moderada");
        }
        return frequentLocationRepository.save(frequentLocation);
    }

    @Transactional
    public FrequentLocation update(Long id, FrequentLocation frequentLocation, Long currentUserId) {
        FrequentLocation existing = getById(id, currentUserId);
        
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
    public void delete(Long id, Long currentUserId) {
        FrequentLocation existing = getById(id, currentUserId);
        frequentLocationRepository.deleteById(id);
    }
}
