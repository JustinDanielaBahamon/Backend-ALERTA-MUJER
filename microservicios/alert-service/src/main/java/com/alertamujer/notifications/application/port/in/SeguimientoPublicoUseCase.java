package com.alertamujer.notifications.application.port.in;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seguimiento público de una alerta con el enlace del correo (sin iniciar sesión). Si el enlace no
 * existe, se revocó o venció, responde ResourceNotFoundException.
 */
public interface SeguimientoPublicoUseCase {

    /** Crea un enlace para la alerta y devuelve el código (no se puede recuperar después). */
    String crearEnlace(Long alertId);

    TrackingView consultar(String code, LocalDateTime ahora);

    /**
     * Como consultar, y además deja constancia de que ese visitante (identificador anónimo del
     * navegador o la app) abrió el seguimiento: la usuaria ve cuántos la están viendo.
     */
    TrackingView consultar(String code, String visitante, LocalDateTime ahora);

    /** Archivo de una grabación de la alerta del enlace. */
    EvidenceFile evidence(String code, Long evidenciaId, LocalDateTime ahora);

    List<AlertMessageView> mensajes(String code, Long afterId, LocalDateTime ahora);

    /** Mensaje del contacto; con límite por enlace para que nadie llene el chat. */
    AlertMessageView enviarMensaje(String code, String name, String text, LocalDateTime ahora);

    /** Solo el nombre de pila de la usuaria: el enlace puede reenviarse. */
    record TrackingView(String firstName, String status, boolean active, LocalDateTime startedAt,
                        LocalDateTime endedAt, List<LocationPoint> route, String emergencyNumber,
                        List<EvidenciaPublica> evidence) {
        public LocationPoint lastPoint() {
            return route.isEmpty() ? null : route.get(route.size() - 1);
        }
    }

    /** Evidencia que ve el contacto: se abre con /api/public/tracking/{codigo}/evidence/{id}. */
    record EvidenciaPublica(Long id, String mediaType, String contentType, LocalDateTime createdAt) {}
}
