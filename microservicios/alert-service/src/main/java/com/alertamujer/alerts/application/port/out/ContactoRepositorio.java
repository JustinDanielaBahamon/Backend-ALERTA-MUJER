package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: los contactos de emergencia. */
public interface ContactoRepositorio {

    List<EmergencyContact> todos();

    Optional<EmergencyContact> buscar(Long id);

    List<EmergencyContact> buscarVarios(Collection<Long> ids);

    List<EmergencyContact> deLaUsuaria(Long perfil);

    EmergencyContact guardar(EmergencyContact contacto);

    void eliminar(Long id);
}
