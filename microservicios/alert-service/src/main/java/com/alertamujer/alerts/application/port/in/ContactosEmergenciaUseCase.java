package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.DatosContacto;
import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.List;
import java.util.Optional;

/** Contactos de emergencia de la usuaria. */
public interface ContactosEmergenciaUseCase {

    /** Todos (solo administración). */
    List<EmergencyContact> todos();

    /** Vacío si no existe o es de otra usuaria. */
    Optional<EmergencyContact> uno(Long id, Long userId);

    /**
     * Contactos de una usuaria. Los clientes envían el id de usuaria (users.id); también se acepta
     * el id de perfil propio. La administración puede ver los de cualquiera.
     */
    List<EmergencyContact> deUsuaria(Long idPedido, Long userId, boolean admin);

    EmergencyContact crear(DatosContacto datos, Long userId);

    EmergencyContact actualizar(Long id, DatosContacto datos, Long userId);

    void eliminar(Long id, Long userId);
}
