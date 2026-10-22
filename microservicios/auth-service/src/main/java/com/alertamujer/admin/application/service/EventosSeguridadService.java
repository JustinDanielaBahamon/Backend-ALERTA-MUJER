package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.RegistrarEventoSeguridadUseCase;
import com.alertamujer.admin.application.port.out.AuditoriaRepositorio;
import com.alertamujer.admin.application.port.out.ContextoPeticionPort;
import com.alertamujer.admin.domain.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Guarda los eventos de sesión de las cuentas admin en la auditoría (tipo SESSION). */
@Service
public class EventosSeguridadService implements RegistrarEventoSeguridadUseCase {

    private static final Logger log = LoggerFactory.getLogger(EventosSeguridadService.class);

    private final AuditoriaRepositorio repository;
    private final ContextoPeticionPort peticion;

    public EventosSeguridadService(AuditoriaRepositorio repository, ContextoPeticionPort peticion) {
        this.repository = repository;
        this.peticion = peticion;
    }

    /**
     * Transacción propia: un intento fallido se guarda aunque el login termine en excepción, y un
     * fallo al auditar nunca impide iniciar sesión.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long accountId, String accion, String detalle) {
        try {
            repository.save(AuditLog.eventoDeSesion(accountId, accion, detalle, peticion.dispositivo(), peticion.ip(),
                    peticion.origen()));
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar el evento de seguridad {}: {}", accion, e.getMessage());
        }
    }
}
