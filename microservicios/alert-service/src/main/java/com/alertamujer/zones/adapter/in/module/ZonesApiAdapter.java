package com.alertamujer.zones.adapter.in.module;

import com.alertamujer.zones.api.ZonesApi;
import com.alertamujer.zones.application.port.in.ZonesUseCase;
import org.springframework.stereotype.Component;

/** Input adapter: the ZonesApi contract used by the Auth Service panel (through /internal/rpc). */
@Component
class ZonesApiAdapter implements ZonesApi {

    private final ZonesUseCase zones;

    ZonesApiAdapter(ZonesUseCase zones) {
        this.zones = zones;
    }

    @Override
    public long countActiveZones() {
        return zones.countActive();
    }
}
