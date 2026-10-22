package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: los contactos de emergencia. */
public interface ContactoRepositorio {

    List<EmergencyContact> all();

    Optional<EmergencyContact> find(Long id);

    List<EmergencyContact> buscarVarios(Collection<Long> ids);

    List<EmergencyContact> ofUser(Long profile);

    EmergencyContact save(EmergencyContact contacto);

    void delete(Long id);
}
