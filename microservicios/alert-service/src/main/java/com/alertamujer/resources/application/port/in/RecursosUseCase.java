package com.alertamujer.resources.application.port.in;

import com.alertamujer.resources.domain.model.DatosRecurso;
import com.alertamujer.resources.domain.model.EmergencyResource;

import java.util.List;
import java.util.Optional;

/** Recursos de ayuda: los ve cualquier usuaria con sesión y los edita la administración. */
public interface RecursosUseCase {

    List<EmergencyResource> todos();

    Optional<EmergencyResource> uno(Long id);

    List<EmergencyResource> deCiudad(String ciudad);

    List<EmergencyResource> deTipo(String type);

    EmergencyResource create(DatosRecurso data);

    EmergencyResource reemplazar(Long id, DatosRecurso data);

    void delete(Long id);
}
