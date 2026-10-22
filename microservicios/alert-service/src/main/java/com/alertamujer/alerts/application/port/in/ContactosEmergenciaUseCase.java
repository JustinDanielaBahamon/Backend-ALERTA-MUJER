package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.DatosContacto;
import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Contactos de emergencia de la usuaria. */
public interface ContactosEmergenciaUseCase {

    /** Vacío si no existe o es de otra usuaria. */
    Optional<EmergencyContact> uno(Long id, Long userId);

    /** Solo los propios: idPedido puede ser el users.id o el user_profile.id de la usuaria (S2: la administración no los ve). */
    List<EmergencyContact> deUsuaria(Long idPedido, Long userId);

    EmergencyContact crear(DatosContacto datos, Long userId);

    EmergencyContact actualizar(Long id, DatosContacto datos, Long userId);

    void eliminar(Long id, Long userId);

    /**
     * Si a cada contacto le puede llegar el aviso en el celular: "app" (tiene cuenta y un celular con
     * notificaciones registrado), "account" (tiene cuenta pero ningún celular) o "none" (solo correo).
     */
    Map<Long, String> estadoApp(Long userId);

    String CON_APP = "app";
    String SIN_CELULAR = "account";
    String SIN_CUENTA = "none";
}
