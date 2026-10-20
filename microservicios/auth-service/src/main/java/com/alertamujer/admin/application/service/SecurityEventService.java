package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.shared.web.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Eventos de seguridad de las cuentas de administración (inicios y cierres de sesión,
 * intentos fallidos, cambio de contraseña). Se guardan en admin.audit_log con tipo SESSION.
 * Solo se registran cuentas admin: las usuarias de la app no llenan la auditoría del panel.
 */
@Service
public class SecurityEventService {

    public static final String TIPO = "SESSION";
    public static final String LOGIN_SUCCESS = AdminSessionEvent.LOGIN_SUCCESS;
    public static final String LOGIN_FAILED = AdminSessionEvent.LOGIN_FAILED;
    public static final String LOGOUT = AdminSessionEvent.LOGOUT;
    public static final String PASSWORD_CHANGED = AdminSessionEvent.PASSWORD_CHANGED;

    private static final Logger log = LoggerFactory.getLogger(SecurityEventService.class);

    private final AuditLogRepository auditLogRepository;

    public SecurityEventService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** Registra los eventos de sesión que publica el módulo identity. */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alEventoDeSesion(AdminSessionEvent evento) {
        registrar(evento.accountId(), evento.action(), evento.detail());
    }

    /**
     * Transacción propia: un intento fallido se guarda aunque el login termine en excepción,
     * y un fallo al auditar nunca impide iniciar sesión.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long accountId, String accion, String detalle) {
        try {
            AuditLog r = new AuditLog();
            r.setAccountId(accountId);
            r.setAction(accion);
            r.setEntityType(TIPO);
            String dispositivo = RequestInfo.dispositivo();
            String texto = detalle == null ? dispositivo : dispositivo == null ? detalle : detalle + " · " + dispositivo;
            r.setDescription(texto == null ? null : texto.length() > 500 ? texto.substring(0, 500) : texto);
            r.setIpAddress(RequestInfo.ip());
            r.setSource(RequestInfo.origen());
            auditLogRepository.save(r);
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar el evento de seguridad {}: {}", accion, e.getMessage());
        }
    }
}
