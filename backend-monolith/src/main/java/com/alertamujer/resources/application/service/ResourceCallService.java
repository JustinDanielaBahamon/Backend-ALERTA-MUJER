package com.alertamujer.resources.application.service;

import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.resources.infrastructure.repository.EmergencyResourceRepository;
import com.alertamujer.resources.infrastructure.repository.ResourceCallRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceCallService {

    private final ResourceCallRepository resourceCallRepository;
    private final EmergencyResourceRepository emergencyResourceRepository;
    private final UserProfileRepository userProfileRepository;

    public ResourceCallService(ResourceCallRepository resourceCallRepository,
                               EmergencyResourceRepository emergencyResourceRepository,
                               UserProfileRepository userProfileRepository) {
        this.resourceCallRepository = resourceCallRepository;
        this.emergencyResourceRepository = emergencyResourceRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // El JWT trae users.id; las llamadas se guardan por user_profile.id.
    private Long profileIdOf(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private ResourceCall owned(Long id, Long currentUserId) {
        ResourceCall call = resourceCallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        if (!call.getUserProfileId().equals(profileIdOf(currentUserId))) {
            throw new AccessDeniedException("No puedes acceder a llamadas de otro usuario");
        }
        return call;
    }

    public List<ResourceCall> getAllCalls() {
        return resourceCallRepository.findAll();
    }

    public Optional<ResourceCall> getCallById(Long id, Long currentUserId) {
        Long profileId = profileIdOf(currentUserId);
        return resourceCallRepository.findById(id).filter(call -> call.getUserProfileId().equals(profileId));
    }

    /** Llamadas de la usuaria autenticada, de la más reciente a la más antigua. */
    public List<ResourceCall> getMyCalls(Long currentUserId) {
        return resourceCallRepository.findByUserProfileIdOrderByStartedAtDesc(profileIdOf(currentUserId));
    }

    public List<ResourceCall> getCallsByUser(Long userId) {
        return getMyCalls(userId);
    }

    public List<ResourceCall> getCallsByResource(Long emergencyResourceId) {
        return resourceCallRepository.findByEmergencyResourceId(emergencyResourceId);
    }

    public List<ResourceCall> getCallsByAlert(Long alertId) {
        return resourceCallRepository.findByAlertId(alertId);
    }

    /** Registra que la usuaria marcó a un recurso. La dueña sale del token, nunca del body. */
    public ResourceCall createCall(ResourceCall request, Long currentUserId) {
        if (request.getEmergencyResourceId() == null) {
            throw new IllegalArgumentException("Indica a qué recurso se llamó");
        }
        var resource = emergencyResourceRepository.findById(request.getEmergencyResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", request.getEmergencyResourceId()));

        ResourceCall call = new ResourceCall();
        call.setUserProfileId(profileIdOf(currentUserId));
        call.setEmergencyResourceId(resource.getId());
        call.setAlertId(request.getAlertId());
        call.setDeviceId(request.getDeviceId());
        String marcado = request.getTelephoneDialed();
        call.setTelephoneDialed(marcado != null && !marcado.isBlank() ? marcado.trim() : resource.getTelephone());
        if (call.getTelephoneDialed() == null) throw new IllegalArgumentException("El recurso no tiene teléfono");
        call.setStatus(request.getStatus() != null ? request.getStatus() : "initiated");
        call.setStartedAt(LocalDateTime.now());
        return resourceCallRepository.save(call);
    }

    public ResourceCall updateCall(Long id, ResourceCall callDetails, Long currentUserId) {
        ResourceCall call = owned(id, currentUserId);
        if (callDetails.getStatus() != null) call.setStatus(callDetails.getStatus());
        if (callDetails.getEndedAt() != null) call.setEndedAt(callDetails.getEndedAt());
        if (callDetails.getDurationSeconds() != null) call.setDurationSeconds(callDetails.getDurationSeconds());
        return resourceCallRepository.save(call);
    }

    public void deleteCall(Long id, Long currentUserId) {
        resourceCallRepository.delete(owned(id, currentUserId));
    }
}
