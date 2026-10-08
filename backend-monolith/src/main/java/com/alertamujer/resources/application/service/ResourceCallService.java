package com.alertamujer.resources.application.service;

import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.resources.infrastructure.repository.ResourceCallRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
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

    public Optional<ResourceCall> getCallById(Long id, Long currentUserId) {
        return resourceCallRepository.findById(id)
                .filter(call -> call.getUserProfileId().equals(currentUserId));
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

    public ResourceCall createCall(ResourceCall call, Long currentUserId) {
        if (!call.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes registrar llamadas para otro usuario");
        }
        return resourceCallRepository.save(call);
    }

    public ResourceCall updateCall(Long id, ResourceCall callDetails, Long currentUserId) {
        ResourceCall call = resourceCallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        
        if (!call.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes modificar llamadas de otro usuario");
        }
        
        call.setStatus(callDetails.getStatus());
        call.setStartedAt(callDetails.getStartedAt());
        call.setEndedAt(callDetails.getEndedAt());
        call.setDurationSeconds(callDetails.getDurationSeconds());
        
        return resourceCallRepository.save(call);
    }

    public void deleteCall(Long id, Long currentUserId) {
        ResourceCall call = resourceCallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        
        if (!call.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes eliminar llamadas de otro usuario");
        }
        
        resourceCallRepository.deleteById(id);
    }
}
