package com.alertamujer.alerts.application.port.in;

/** Al eliminarse una cuenta se borran los datos personales que guarda Alert Service. */
public interface DeleteUserDataUseCase {

    void deleteDataOf(Long userProfileId);
}
