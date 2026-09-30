package com.alertamujer.resources.application.service;

import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.resources.infrastructure.repository.ResourceCallRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceCallService {

    private final ResourceCallRepository resourceCallRepository;

    public ResourceCallService(ResourceCallRepository resourceCallRepository) {
        this.resourceCallRepository = resourceCallRepository;
    }

    public List<ResourceCall> getAllCalls() {
        return resourceCallRepository.findAll();
    }

    public Optional<ResourceCall> getCallById(Long id) {
        return resourceCallRepository.findById(id);
    }

    public List<ResourceCall> getCallsByUserProfile(Long userProfileId) {
        return resourceCallRepository.findByUserProfileId(userProfileId);
    }

    public List<ResourceCall> getCallsByResource(Long emergencyResourceId) {
        return resourceCallRepository.findByEmergencyResourceId(emergencyResourceId);
    }

    public List<ResourceCall> getCallsByAlert(Long alertId) {
        return resourceCallRepository.findByAlertId(alertId);
    }

    public ResourceCall createCall(ResourceCall call) {
        return resourceCallRepository.save(call);
    }

    public ResourceCall updateCall(Long id, ResourceCall callDetails) {
        ResourceCall call = resourceCallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource call not found"));
        
        call.setStatus(callDetails.getStatus());
        call.setStartedAt(callDetails.getStartedAt());
        call.setEndedAt(callDetails.getEndedAt());
        call.setDurationSeconds(callDetails.getDurationSeconds());
        
        return resourceCallRepository.save(call);
    }

    public void deleteCall(Long id) {
        resourceCallRepository.deleteById(id);
    }
}
