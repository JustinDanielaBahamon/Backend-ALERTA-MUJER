package com.alertamujer.settings.application.port.in;

import com.alertamujer.settings.domain.model.SettingsChanges;
import com.alertamujer.settings.domain.model.SystemConfiguration;

/** La administración consulta y cambia los parámetros de la app. */
public interface ManageSettingsUseCase {

    SystemConfiguration get();

    SystemConfiguration update(SettingsChanges changes, Long adminUserId);
}
