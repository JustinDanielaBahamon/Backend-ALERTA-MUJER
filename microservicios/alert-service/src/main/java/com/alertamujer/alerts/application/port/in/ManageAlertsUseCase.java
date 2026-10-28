package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.domain.model.Alert;

import java.util.List;

/** Admin panel use cases. */
public interface ManageAlertsUseCase {

    /** All alerts with the user who activated them, newest to oldest. */
    List<AdminAlertView> listAll();

    /** Changes the status of any alert, for example to "resolved" (attended). */
    Alert changeStatus(Long id, String status);

    /**
     * Detail for the panel: last location (only of active or attended alerts, audit S2) and
     * delivery status to each contact, without their name or email.
     */
    AdminDetail detail(Long id);

    record LastLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}

    record AdminChannel(String channel, String status, LocalDateTime sentAt, LocalDateTime deliveredAt) {}

    /** contacto: number of the contact in the alert (1, 2...). relationship: kinship, if any. */
    record AdminDelivery(@com.fasterxml.jackson.annotation.JsonProperty("contacto") int contact, String relationship, List<AdminChannel> channels) {}

    /** ubicacionVisible: false if the alert was cancelled or expired (the administration does not see its route). */
    record AdminDetail(@com.fasterxml.jackson.annotation.JsonProperty("ubicacionVisible") boolean locationVisible, @com.fasterxml.jackson.annotation.JsonProperty("puntos") int points, @com.fasterxml.jackson.annotation.JsonProperty("ultimaUbicacion") LastLocation lastLocation, @com.fasterxml.jackson.annotation.JsonProperty("envios") List<AdminDelivery> deliveries) {}
}
