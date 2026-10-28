package com.alertamujer.alerts.application.port.out;

import java.math.BigDecimal;

/** Output port: whether a point is inside a zone marked as high risk on the map. */
public interface RiskZonesPort {
    boolean isHighRisk(BigDecimal latitude, BigDecimal longitude);
}
