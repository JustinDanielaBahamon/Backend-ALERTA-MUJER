package com.alertamujer.settings.application.port.in;

import com.alertamujer.settings.domain.model.SettingsChanges;
import com.alertamujer.settings.domain.model.SystemConfiguration;

/** The administration queries and changes the app parameters. */
public interface ManageSettingsUseCase {

    SystemConfiguration get();

    SystemConfiguration update(SettingsChanges changes, Long adminUserId);
}
