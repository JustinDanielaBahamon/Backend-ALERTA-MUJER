package com.alertamujer.alerts.application.port.out;

/** Output port: deletion of a user's personal data in Alert Service. */
public interface UserDataRepository {

    /** Returns how many rows were deleted or cleared. */
    int deleteOf(Long userProfileId);
}
