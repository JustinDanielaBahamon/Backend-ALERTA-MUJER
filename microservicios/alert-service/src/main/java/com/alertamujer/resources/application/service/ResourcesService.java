package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.in.ResourcesUseCase;
import com.alertamujer.resources.application.port.out.ResourceRepository;
import com.alertamujer.resources.domain.model.ResourceData;
import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Help resources. Only knows the resources port. */
@Service
public class ResourcesService implements ResourcesUseCase {

    private final ResourceRepository repository;

    public ResourcesService(ResourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EmergencyResource> all() {
        return repository.all();
    }

    @Override
    public Optional<EmergencyResource> one(Long id) {
        return repository.find(id);
    }

    @Override
    public List<EmergencyResource> ofCity(String city) {
        return repository.ofCity(city);
    }

    @Override
    public List<EmergencyResource> ofType(String type) {
        return repository.ofType(type);
    }

    @Override
    @Transactional
    public EmergencyResource create(ResourceData data) {
        return repository.save(EmergencyResource.created(data));
    }

    @Override
    @Transactional
    public EmergencyResource replace(Long id, ResourceData data) {
        EmergencyResource r = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", id));
        r.replace(data);
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(id);
    }
}
