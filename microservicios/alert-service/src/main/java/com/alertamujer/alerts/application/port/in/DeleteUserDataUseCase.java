package com.alertamujer.alerts.application.port.in;

/** When an account is deleted, the personal data Alert Service keeps is erased. */
public interface DeleteUserDataUseCase {

    void deleteDataOf(Long userProfileId);
}
