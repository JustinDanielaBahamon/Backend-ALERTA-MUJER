package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationLogService {

    private final LocationLogRepository locationLogRepository;
    private final UserProfileRepository userProfileRepository;

    public LocationLogService(LocationLogRepository locationLogRepository, UserProfileRepository userProfileRepository) {
        this.locationLogRepository = locationLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<LocationLog> getAll() {
        return locationLogRepository.findAll();
    }

    public List<LocationLog> getByUserProfileId(Long userProfileId) {
        return locationLogRepository.findByUserProfileIdOrderByRecordedAtDesc(userProfileId);
    }

    public List<LocationLog> getByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user: " + userId));
        return locationLogRepository.findByUserProfileIdOrderByRecordedAtDesc(userProfile.getId());
    }

    public LocationLog create(LocationLog locationLog, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user: " + currentUserId));
        
        // Ignorar userProfileId del payload y usar el del JWT
        locationLog.setUserProfileId(userProfile.getId());
        return locationLogRepository.save(locationLog);
    }
}
