package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import com.alertamujer.notifications.application.port.out.CorreosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de salida: arma cada correo con la plantilla de la marca y lo envía con el proveedor configurado. */
@Component
class CorreosAdapter implements CorreosPort {

    private final EmailSender emailSender;
    private final String logoUrl;

    CorreosAdapter(EmailSender emailSender, @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.emailSender = emailSender;
        this.logoUrl = EmailLayout.logoUrl(webBaseUrl);
    }

    @Override
    public void avisoDeAlerta(String para, String nombreContacto, String nombreUsuaria, LocalDateTime inicio,
                              String seguimientoUrl, String mapsUrl, String emergencyNumber) {
        send(AlertEmailTemplate.construir(para, nombreContacto, nombreUsuaria, inicio, seguimientoUrl, mapsUrl,
                emergencyNumber, logoUrl));
    }

    @Override
    public void finDeAlerta(String para, String nombreContacto, String nombreUsuaria, String status, LocalDateTime fin,
                            String emergencyNumber) {
        send(AlertEmailTemplate.construirFin(para, nombreContacto, nombreUsuaria, status, fin, emergencyNumber, logoUrl));
    }

    @Override
    public void codigoRecuperacion(String para, String name, String code, int validityMinutes) {
        send(CodigoRecuperacionTemplate.construir(para, name, code, validityMinutes, logoUrl));
    }

    private void send(EmailMessage message) {
        try {
            emailSender.send(message);
        } catch (EmailSender.EmailException e) {
            throw new CorreoNoEnviadoException(e.getMessage());
        }
    }
}
