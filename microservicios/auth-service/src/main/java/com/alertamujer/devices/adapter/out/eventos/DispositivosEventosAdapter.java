package com.alertamujer.devices.adapter.out.eventos;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.devices.application.port.out.EventosDispositivosPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida: evento de Spring que Auth Service reenvía por HTTP a Alert Service. */
@Component
class DispositivosEventosAdapter implements EventosDispositivosPort {

    private final ApplicationEventPublisher publisher;

    DispositivosEventosAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void nuevoCelular(String nombreCelular, List<String> tokensOtrosCelulares) {
        publisher.publishEvent(new NewPhoneSignedInEvent(nombreCelular, tokensOtrosCelulares));
    }
}
