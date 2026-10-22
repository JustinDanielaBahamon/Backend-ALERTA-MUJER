package com.alertamujer.admin.adapter.out.zones;

import com.alertamujer.admin.application.port.out.ZonesPort;
import com.alertamujer.zones.api.ZonesApi;
import org.springframework.stereotype.Component;

/** Adaptador de salida hacia las zonas de Alert Service (cliente HTTP, /internal/rpc). */
@Component
class AdminZonesAdapter implements ZonesPort {

    private final ZonesApi zonesApi;

    AdminZonesAdapter(ZonesApi zonesApi) {
        this.zonesApi = zonesApi;
    }

    @Override
    public long active() {
        return zonesApi.countActiveZones();
    }
}
