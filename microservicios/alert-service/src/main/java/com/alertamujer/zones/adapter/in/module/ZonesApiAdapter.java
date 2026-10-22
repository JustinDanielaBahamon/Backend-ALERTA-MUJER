package com.alertamujer.zones.adapter.in.module;

import com.alertamujer.zones.api.ZonesApi;
import com.alertamujer.zones.application.port.in.ZonasUseCase;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: el contrato ZonesApi que usa el panel de Auth Service (por /internal/rpc). */
@Component
class ZonesApiAdapter implements ZonesApi {

    private final ZonasUseCase zones;

    ZonesApiAdapter(ZonasUseCase zones) {
        this.zones = zones;
    }

    @Override
    public long countActiveZones() {
        return zones.contarActivas();
    }
}
