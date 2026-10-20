package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.BorrarDatosDeUsuariaUseCase;
import com.alertamujer.alerts.application.port.out.DatosDeUsuariaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Cuenta eliminada: se borran sus contactos de emergencia, ubicaciones, recorridos, lugares
 * frecuentes, mensajes del chat y enlaces de seguimiento. Las alertas quedan como registro anónimo
 * (sin mensaje ni ubicación) para que las estadísticas del panel no cambien.
 */
@Service
public class BorradoDatosUsuariaService implements BorrarDatosDeUsuariaUseCase {

    private static final Logger log = LoggerFactory.getLogger(BorradoDatosUsuariaService.class);

    private final DatosDeUsuariaRepositorio datos;

    public BorradoDatosUsuariaService(DatosDeUsuariaRepositorio datos) {
        this.datos = datos;
    }

    @Override
    public void borrarDatosDe(Long userProfileId) {
        if (userProfileId == null) return;
        int filas = datos.borrarDe(userProfileId);
        log.info("Cuenta eliminada: datos del perfil {} borrados ({} filas)", userProfileId, filas);
    }
}
