package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;

import java.util.List;

/** La administración ve, cambia de estado y borra cualquier dispositivo, con los datos de su dueña. */
public interface AdministrarDispositivosUseCase {

    /** Del usado más recientemente al más antiguo. */
    List<DispositivoConDuena> listAll();

    DispositivoConDuena obtener(Long id);

    /** active, inactive, blocked o lost. */
    DispositivoConDuena cambiarEstado(Long id, String estado);

    void delete(Long id);

    record DispositivoConDuena(Device dispositivo, UserSummary owner) {}
}
