package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.time.LocalDateTime;

public record EmergencyContactResponse(Long id, Long userProfileId, String contactName, String telephone, String email,
                                       String relationship, String photo, LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {

    public static EmergencyContactResponse of(EmergencyContact c) {
        return new EmergencyContactResponse(c.getId(), c.getUserProfileId(), c.getContactName(), c.getTelephone(),
                c.getEmail(), c.getRelationship(), c.getPhoto(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
