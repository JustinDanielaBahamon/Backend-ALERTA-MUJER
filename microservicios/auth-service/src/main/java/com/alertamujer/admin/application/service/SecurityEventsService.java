package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.RecordSecurityEventUseCase;
import com.alertamujer.admin.application.port.out.AuditRepository;
import com.alertamujer.admin.application.port.out.RequestContextPort;
import com.alertamujer.admin.domain.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Guarda los eventos de sesión de las cuentas admin en la auditoría (tipo SESSION). */
@Service
public class SecurityEventsService implements RecordSecurityEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventsService.class);

    private final AuditRepository repository;
    private final RequestContextPort request;

    public SecurityEventsService(AuditRepository repository, RequestContextPort request) {
        this.repository = repository;
        this.request = request;
    }

    /**
     * Transacción propia: un intento fallido se guarda aunque el login termine en excepción, y un
     * fallo al auditar nunca impide iniciar sesión.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long accountId, String action, String detail) {
        try {
            repository.save(AuditLog.sessionEvent(accountId, action, detail, request.device(), request.ip(),
                    request.source()));
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar el evento de seguridad {}: {}", action, e.getMessage());
        }
    }
}
