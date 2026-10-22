package com.alertamujer.devices.adapter.out.events;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.devices.application.port.out.DeviceEventsPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/** Output adapter: Spring event that Auth Service forwards over HTTP to Alert Service. */
@Component
class DeviceEventsAdapter implements DeviceEventsPort {

    private final ApplicationEventPublisher publisher;

    DeviceEventsAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void newPhone(String phoneName, List<String> otherPhoneTokens) {
        publisher.publishEvent(new NewPhoneSignedInEvent(phoneName, otherPhoneTokens));
    }
}
