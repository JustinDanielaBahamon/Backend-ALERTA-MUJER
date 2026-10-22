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
                              String seguimientoUrl, String mapsUrl, String numeroEmergencia) {
        enviar(AlertEmailTemplate.construir(para, nombreContacto, nombreUsuaria, inicio, seguimientoUrl, mapsUrl,
                numeroEmergencia, logoUrl));
    }

    @Override
    public void finDeAlerta(String para, String nombreContacto, String nombreUsuaria, String estado, LocalDateTime fin,
                            String numeroEmergencia) {
        enviar(AlertEmailTemplate.construirFin(para, nombreContacto, nombreUsuaria, estado, fin, numeroEmergencia, logoUrl));
    }

    @Override
    public void codigoRecuperacion(String para, String name, String codigo, int minutosValidez) {
        enviar(CodigoRecuperacionTemplate.construir(para, name, codigo, minutosValidez, logoUrl));
    }

    private void enviar(EmailMessage mensaje) {
        try {
            emailSender.send(mensaje);
        } catch (EmailSender.EmailException e) {
            throw new CorreoNoEnviadoException(e.getMessage());
        }
    }
}
