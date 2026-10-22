package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.DeleteUserDataUseCase;
import com.alertamujer.alerts.application.port.out.UserDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Cuenta eliminada: se borran sus contactos de emergencia, ubicaciones, recorridos, lugares
 * frecuentes, mensajes del chat y enlaces de seguimiento. Las alertas quedan como registro anónimo
 * (sin mensaje ni ubicación) para que las estadísticas del panel no cambien.
 */
@Service
public class UserDataDeletionService implements DeleteUserDataUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserDataDeletionService.class);

    private final UserDataRepository data;

    public UserDataDeletionService(UserDataRepository data) {
        this.data = data;
    }

    @Override
    public void deleteDataOf(Long userProfileId) {
        if (userProfileId == null) return;
        int rows = data.deleteOf(userProfileId);
        log.info("Cuenta eliminada: datos del perfil {} borrados ({} filas)", userProfileId, rows);
    }
}
