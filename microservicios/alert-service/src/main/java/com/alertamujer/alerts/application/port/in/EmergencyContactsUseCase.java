package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.ContactData;
import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The user's emergency contacts. */
public interface EmergencyContactsUseCase {

    /** Empty if it does not exist or belongs to another user. */
    Optional<EmergencyContact> one(Long id, Long userId);

    /** Own ones only: requestedId may be the user's users.id or user_profile.id (S2: the administration does not see them). */
    List<EmergencyContact> ofUser(Long requestedId, Long userId);

    EmergencyContact create(ContactData data, Long userId);

    EmergencyContact update(Long id, ContactData data, Long userId);

    void delete(Long id, Long userId);

    /**
     * Whether each contact can get the notice on her phone: "app" (has an account and a phone with
     * notifications registered), "account" (has an account but no phone) or "none" (email only).
     */
    Map<Long, String> appStatus(Long userId);

    String CON_APP = "app";
    String NO_PHONE = "account";
    String NO_ACCOUNT = "none";
}
