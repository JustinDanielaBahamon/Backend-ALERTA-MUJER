package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.in.CallsUseCase;
import com.alertamujer.resources.application.port.out.IdentityPort;
import com.alertamujer.resources.application.port.out.CallRepository;
import com.alertamujer.resources.application.port.out.ResourceRepository;
import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Calls to the resources: each user only sees and changes her own. */
@Service
public class CallsService implements CallsUseCase {

    private final CallRepository repository;
    private final ResourceRepository resources;
    private final IdentityPort identity;

    public CallsService(CallRepository repository, ResourceRepository resources, IdentityPort identity) {
        this.repository = repository;
        this.resources = resources;
        this.identity = identity;
    }

    @Override
    public List<ResourceCall> all() {
        return repository.all();
    }

    @Override
    public Optional<ResourceCall> one(Long id, Long userId) {
        Long profile = identity.requiredProfile(userId);
        return repository.find(id).filter(c -> c.esDe(profile));
    }

    @Override
    public List<ResourceCall> mine(Long userId) {
        return repository.ofProfile(identity.requiredProfile(userId));
    }

    @Override
    public List<ResourceCall> ofResource(Long resourceId) {
        return repository.ofResource(resourceId);
    }

    @Override
    public List<ResourceCall> ofAlert(Long alertId) {
        return repository.ofAlert(alertId);
    }

    @Override
    @Transactional
    public ResourceCall registrar(Long resourceId, Long alertId, Long deviceId, String dialedNumber, String status, Long userId) {
        if (resourceId == null) throw new IllegalArgumentException("Indica a qué recurso se llamó");
        EmergencyResource resource = resources.find(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", resourceId));
        return repository.save(ResourceCall.newOne(identity.requiredProfile(userId), resource, alertId, deviceId,
                dialedNumber, status, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public ResourceCall update(Long id, String status, LocalDateTime end, Integer durationSeconds, Long userId) {
        ResourceCall c = own(id, userId);
        c.update(status, end, durationSeconds);
        return repository.save(c);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(own(id, userId).getId());
    }

    private ResourceCall own(Long id, Long userId) {
        ResourceCall c = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        if (!c.esDe(identity.requiredProfile(userId))) {
            throw new AccessDeniedException("No puedes acceder a llamadas de otro usuario");
        }
        return c;
    }
}
