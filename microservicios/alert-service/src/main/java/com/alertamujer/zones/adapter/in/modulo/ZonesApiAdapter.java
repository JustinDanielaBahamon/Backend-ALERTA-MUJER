package com.alertamujer.zones.adapter.in.modulo;

import com.alertamujer.zones.api.ZonesApi;
import com.alertamujer.zones.application.port.in.ZonasUseCase;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: el contrato ZonesApi que usa el panel de Auth Service (por /internal/rpc). */
@Component
class ZonesApiAdapter implements ZonesApi {

    private final ZonasUseCase zonas;

    ZonesApiAdapter(ZonasUseCase zonas) {
        this.zonas = zonas;
    }

    @Override
    public long countActiveZones() {
        return zonas.contarActivas();
    }
}
