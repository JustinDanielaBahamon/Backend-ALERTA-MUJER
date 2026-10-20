package com.alertamujer.admin.adapter.out.zones;

import com.alertamujer.admin.application.port.out.ZonasPort;
import com.alertamujer.zones.api.ZonesApi;
import org.springframework.stereotype.Component;

/** Adaptador de salida hacia las zonas de Alert Service (cliente HTTP, /internal/rpc). */
@Component
class AdminZonasAdapter implements ZonasPort {

    private final ZonesApi zonesApi;

    AdminZonasAdapter(ZonesApi zonesApi) {
        this.zonesApi = zonesApi;
    }

    @Override
    public long activas() {
        return zonesApi.countActiveZones();
    }
}
