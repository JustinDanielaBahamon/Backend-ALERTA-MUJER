package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.EnviarCodigoRecuperacionUseCase;
import com.alertamujer.notifications.application.port.out.CorreosPort;
import com.alertamujer.notifications.application.port.out.CorreosPort.CorreoNoEnviadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Envía el código de recuperación de contraseña; un fallo solo queda en el log (Auth ya respondió). */
@Service
public class CodigoRecuperacionService implements EnviarCodigoRecuperacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CodigoRecuperacionService.class);

    private final CorreosPort correos;

    public CodigoRecuperacionService(CorreosPort correos) {
        this.correos = correos;
    }

    @Override
    public void enviar(String correo, String nombre, String codigo, int minutosValidez) {
        try {
            correos.codigoRecuperacion(correo, nombre, codigo, minutosValidez);
        } catch (CorreoNoEnviadoException e) {
            log.warn("No se pudo enviar el código de recuperación: {}", e.getMessage());
        }
    }
}
