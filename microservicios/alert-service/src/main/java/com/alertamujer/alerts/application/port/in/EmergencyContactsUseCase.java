package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.ContactData;
import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Contactos de emergencia de la usuaria. */
public interface EmergencyContactsUseCase {

    /** Vacío si no existe o es de otra usuaria. */
    Optional<EmergencyContact> one(Long id, Long userId);

    /** Solo los propios: idPedido puede ser el users.id o el user_profile.id de la usuaria (S2: la administración no los ve). */
    List<EmergencyContact> ofUser(Long requestedId, Long userId);

    EmergencyContact create(ContactData data, Long userId);

    EmergencyContact update(Long id, ContactData data, Long userId);

    void delete(Long id, Long userId);

    /**
     * Si a cada contacto le puede llegar el aviso en el celular: "app" (tiene cuenta y un celular con
     * notificaciones registrado), "account" (tiene cuenta pero ningún celular) o "none" (solo correo).
     */
    Map<Long, String> appStatus(Long userId);

    String CON_APP = "app";
    String NO_PHONE = "account";
    String NO_ACCOUNT = "none";
}
