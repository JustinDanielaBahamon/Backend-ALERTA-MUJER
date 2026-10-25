package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Automatic protection: her phone watches over her when she leaves one of her frequent places,
 * without her starting a trip. The phone uses its own key (see ProtectionDevice).
 */
public interface AutoProtectionUseCase {

    /** Turns it on in this phone and returns its key (only this time). Signed-in user. */
    String enable(Long userId, Long deviceId);

    /** Turns it off in this phone: its key stops working. Signed-in user. */
    void disable(Long userId, Long deviceId);

    boolean enabled(Long userId, Long deviceId);

    /**
     * She left a frequent place. If she already has a trip or a protection going on, that one is
     * returned (nothing new starts).
     */
    SafeTrip startAuto(String key, BigDecimal latitude, BigDecimal longitude);

    /** Signal of life of the automatic protection. An unknown key or another person's trip fails. */
    SafeTrip autoHeartbeat(String key, Long tripId, AutoHeartbeat point);

    /** She arrived at a frequent place, stopped somewhere or stopped it from the notification. */
    SafeTrip endAuto(String key, Long tripId);

    /** She stopped inside a risk zone and did not answer "¿Todo bien?": her contacts are alerted. */
    SafeTrip noAnswer(String key, Long tripId);

    record AutoHeartbeat(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt,
                         boolean lowBattery) {}
}
