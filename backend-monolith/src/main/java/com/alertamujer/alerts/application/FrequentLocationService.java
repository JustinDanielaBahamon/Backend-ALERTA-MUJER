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

    public FrequentLocationService(FrequentLocationRepository frequentLocationRepository,
                                   UserProfileRepository userProfileRepository) {
        this.frequentLocationRepository = frequentLocationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // El JWT trae users.id; las ubicaciones se guardan por user_profile.id.
    private Long profileIdOf(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    public List<FrequentLocation> getActiveByUserId(Long userId) {
        return getActiveByUserProfileId(profileIdOf(userId));
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
        return frequentLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FrequentLocation", id));
    }

    public FrequentLocation getById(Long id, Long currentUserId) {
        FrequentLocation location = getById(id);
        if (!location.getUserProfileId().equals(profileIdOf(currentUserId))) {
            throw new AccessDeniedException("No puedes acceder a ubicaciones frecuentes de otro usuario");
        }
        return location;
    }

    @Transactional
    public FrequentLocation create(FrequentLocation frequentLocation, Long currentUserId) {
        // El propietario sale del JWT; se ignora el userProfileId del body (evita IDOR).
        frequentLocation.setId(null);
        frequentLocation.setUserProfileId(profileIdOf(currentUserId));
        frequentLocation.setCreatedAt(java.time.LocalDateTime.now());
        if (frequentLocation.getRiskLevel() == null || frequentLocation.getRiskLevel().isBlank()) {
            frequentLocation.setRiskLevel("moderada");
        }
        if (frequentLocation.getIsActive() == null) {
            frequentLocation.setIsActive(true);
        }
        return frequentLocationRepository.save(frequentLocation);
    }

    // Actualización parcial: solo se cambian los campos que llegan (web usa PATCH y el móvil
    // envía únicamente lo modificado). Antes un campo ausente borraba address/city/notes.
    @Transactional
    public FrequentLocation update(Long id, FrequentLocation changes, Long currentUserId) {
        FrequentLocation existing = getById(id, currentUserId);

        if (changes.getName() != null && !changes.getName().isBlank()) existing.setName(changes.getName());
        if (changes.getAddress() != null) existing.setAddress(changes.getAddress());
        if (changes.getCity() != null) existing.setCity(changes.getCity());
        if (changes.getLatitude() != null) existing.setLatitude(changes.getLatitude());
        if (changes.getLongitude() != null) existing.setLongitude(changes.getLongitude());
        if (changes.getNotes() != null) existing.setNotes(changes.getNotes());
        if (changes.getRiskLevel() != null && !changes.getRiskLevel().isBlank()) existing.setRiskLevel(changes.getRiskLevel());
        if (changes.getIsActive() != null) existing.setIsActive(changes.getIsActive());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        return frequentLocationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        FrequentLocation existing = getById(id, currentUserId);
        frequentLocationRepository.deleteById(id);
    }
}
