package com.alertamujer.settings.application.port.in;

import com.alertamujer.settings.domain.model.CambiosAjustes;
import com.alertamujer.settings.domain.model.SystemConfiguration;

/** La administración consulta y cambia los parámetros de la app. */
public interface AdministrarAjustesUseCase {

    SystemConfiguration obtener();

    SystemConfiguration actualizar(CambiosAjustes cambios, Long adminUserId);
}
