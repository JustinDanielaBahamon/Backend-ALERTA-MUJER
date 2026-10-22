package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.AvisarContactosUseCase;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.AjustesPort;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.CorreosPort;
import com.alertamujer.notifications.application.port.out.CorreosPort.CorreoNoEnviadoException;
import com.alertamujer.notifications.application.port.out.DispositivosPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Avisos a los contactos de emergencia al activar y al terminar una alerta. Solo conoce puertos. */
@Service
public class AvisosAlertaService implements AvisarContactosUseCase {

    private static final Logger log = LoggerFactory.getLogger(AvisosAlertaService.class);

    /** Canales que registra el módulo alerts para cada contacto. */
    static final String CANAL_CORREO = "email";
    static final String CANAL_PUSH = "push";

    private final AlertsPort alerts;
    private final IdentityPort identidad;
    private final DispositivosPort dispositivos;
    private final AjustesPort ajustes;
    private final SeguimientoPublicoUseCase seguimiento;
    private final CorreosPort correos;
    private final PushPort push;
    private final String webBaseUrl;

    public AvisosAlertaService(AlertsPort alerts, IdentityPort identidad, DispositivosPort dispositivos,
                               AjustesPort ajustes, SeguimientoPublicoUseCase seguimiento, CorreosPort correos,
                               PushPort push, @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.alerts = alerts;
        this.identidad = identidad;
        this.dispositivos = dispositivos;
        this.ajustes = ajustes;
        this.seguimiento = seguimiento;
        this.correos = correos;
        this.push = push;
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void alertaActivada(Long alertId, Long perfilUsuaria, LocalDateTime inicio) {
        List<AlertRecipient> envios = alerts.destinatarios(alertId);
        if (envios.isEmpty()) return;
        AlertSummary alert = alerts.find(alertId).orElse(null);
        String name = identidad.nombreCompletoDelPerfil(perfilUsuaria).orElse(null);
        Map<Long, String> correoPorContacto = correosPorContacto(envios);
        // Un enlace por alerta: lo usan el correo (página web) y el push (pantalla de la app)
        String codigo = seguimiento.crearEnlace(alertId);
        String url = webBaseUrl + "/seguimiento/" + codigo;
        for (AlertRecipient envio : envios) {
            if (CANAL_CORREO.equals(envio.channel())) {
                enviarCorreo(envio, name, inicio, alert, url);
            } else if (CANAL_PUSH.equals(envio.channel())) {
                enviarPush(envio, correoPorContacto.get(envio.emergencyContactId()), name, alertId, codigo);
            }
        }
    }

    @Override
    public void alertaTerminada(Long alertId, Long perfilUsuaria, String estado, LocalDateTime fin) {
        List<AlertRecipient> envios = alerts.destinatarios(alertId);
        if (envios.isEmpty()) return;
        String name = identidad.nombreCompletoDelPerfil(perfilUsuaria).orElse(null);
        Map<Long, String> correoPorContacto = correosPorContacto(envios);
        for (AlertRecipient envio : envios) {
            try {
                if (CANAL_CORREO.equals(envio.channel())) {
                    correos.finDeAlerta(envio.destination(), envio.contactName(), name, estado, fin, ajustes.numeroEmergencia());
                } else if (CANAL_PUSH.equals(envio.channel()) && envio.emergencyContactId() != null) {
                    List<String> tokens = tokensDelContacto(correoPorContacto.get(envio.emergencyContactId()));
                    if (!tokens.isEmpty()) {
                        push.enviar(tokens, Map.of(
                                "tipo", "ALERTA_FINALIZADA",
                                "alertaId", String.valueOf(alertId),
                                "nombre", name == null ? "" : name,
                                "estado", estado));
                    }
                }
            } catch (CorreoNoEnviadoException e) {
                log.warn("No se pudo enviar el aviso de fin de la alerta {} al envío {}: {}", alertId, envio.id(), e.getMessage());
            }
        }
    }

    /** Enlace de Google Maps con el punto (para el correo, como respaldo si la página tarda). */
    static String googleMaps(BigDecimal lat, BigDecimal lng) {
        return "https://www.google.com/maps/search/?api=1&query=" + lat.toPlainString() + "," + lng.toPlainString();
    }

    private void enviarCorreo(AlertRecipient envio, String name, LocalDateTime inicio, AlertSummary alert, String url) {
        String maps = alert != null && alert.latitude() != null && alert.longitude() != null
                ? googleMaps(alert.latitude(), alert.longitude()) : null;
        try {
            correos.avisoDeAlerta(envio.destination(), envio.contactName(), name, inicio, url, maps, ajustes.numeroEmergencia());
            alerts.registrarEntrega(envio.id(), "sent", null);
        } catch (CorreoNoEnviadoException e) {
            alerts.registrarEntrega(envio.id(), "failed", e.getMessage());
        }
    }

    private void enviarPush(AlertRecipient envio, String correoContacto, String name, Long alertId, String codigo) {
        List<String> tokens = tokensDelContacto(correoContacto);
        if (tokens.isEmpty()) {
            alerts.registrarEntrega(envio.id(), "failed", "El contacto no tiene la app con notificaciones activas");
            return;
        }
        List<String> fallidos = push.enviar(tokens, Map.of(
                "tipo", "ALERTA_SOS",
                "alertaId", String.valueOf(alertId),
                "nombre", name == null ? "" : name,
                "codigo", codigo));
        if (fallidos.size() == tokens.size()) {
            alerts.registrarEntrega(envio.id(), "failed", "FCM rechazó el envío a todos los dispositivos");
        } else {
            alerts.registrarEntrega(envio.id(), "sent", null);
        }
    }

    // El correo de cada contacto sirve también para saber si tiene la app (push)
    private static Map<Long, String> correosPorContacto(List<AlertRecipient> envios) {
        return envios.stream()
                .filter(e -> CANAL_CORREO.equals(e.channel()))
                .collect(Collectors.toMap(AlertRecipient::emergencyContactId, AlertRecipient::destination, (a, b) -> a));
    }

    private List<String> tokensDelContacto(String correo) {
        return identidad.usuariaConCorreo(correo).map(dispositivos::tokensDe).orElse(List.of());
    }
}
