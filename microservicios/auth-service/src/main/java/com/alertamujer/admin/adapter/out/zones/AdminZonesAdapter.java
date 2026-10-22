package com.alertamujer.admin.adapter.out.zones;

import com.alertamujer.admin.application.port.out.ZonesPort;
import com.alertamujer.zones.api.ZonesApi;
import org.springframework.stereotype.Component;

/** Output adapter to the Alert Service zones (HTTP client, /internal/rpc). */
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
