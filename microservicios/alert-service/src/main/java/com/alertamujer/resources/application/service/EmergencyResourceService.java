package com.alertamujer.resources.application.service;

import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.resources.infrastructure.repository.EmergencyResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyResourceService {

    private final EmergencyResourceRepository emergencyResourceRepository;

    public EmergencyResourceService(EmergencyResourceRepository emergencyResourceRepository) {
        this.emergencyResourceRepository = emergencyResourceRepository;
    }

    public List<EmergencyResource> getAllResources() {
        return emergencyResourceRepository.findAll();
    }

    public Optional<EmergencyResource> getResourceById(Long id) {
        return emergencyResourceRepository.findById(id);
    }

    public List<EmergencyResource> getResourcesByCity(String city) {
        return emergencyResourceRepository.findByCity(city);
    }

    public List<EmergencyResource> getResourcesByType(String type) {
        return emergencyResourceRepository.findByResourceType(type);
    }

    public List<EmergencyResource> getActiveResources() {
        return emergencyResourceRepository.findByIsActiveTrue();
    }

    public EmergencyResource createResource(EmergencyResource resource) {
        return emergencyResourceRepository.save(resource);
    }

    public EmergencyResource updateResource(Long id, EmergencyResource resourceDetails) {
        EmergencyResource resource = emergencyResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency resource not found"));
        
        resource.setName(resourceDetails.getName());
        resource.setResourceType(resourceDetails.getResourceType());
        resource.setTelephone(resourceDetails.getTelephone());
        resource.setSecondaryTelephone(resourceDetails.getSecondaryTelephone());
        resource.setEmail(resourceDetails.getEmail());
        resource.setAddress(resourceDetails.getAddress());
        resource.setCity(resourceDetails.getCity());
        resource.setLatitude(resourceDetails.getLatitude());
        resource.setLongitude(resourceDetails.getLongitude());
        resource.setDescription(resourceDetails.getDescription());
        resource.setIsActive(resourceDetails.getIsActive());
        
        return emergencyResourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        emergencyResourceRepository.deleteById(id);
    }
}
