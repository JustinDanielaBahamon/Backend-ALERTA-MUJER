package com.alertamujer.alerts.adapter.out.zones;

import com.alertamujer.alerts.application.port.out.RiskZonesPort;
import com.alertamujer.zones.application.port.in.ZonesUseCase;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Output adapter towards the zones module of this same service. */
@Component
class RiskZonesAdapter implements RiskZonesPort {

    private static final double EARTH_RADIUS_M = 6_371_000;

    private final ZonesUseCase zones;

    RiskZonesAdapter(ZonesUseCase zones) {
        this.zones = zones;
    }

    @Override
    public boolean isHighRisk(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) return false;
        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();
        return zones.all().stream()
                .filter(z -> !Boolean.FALSE.equals(z.getIsActive()) && "high".equals(z.getRiskLevel()))
                .filter(z -> z.getLatitude() != null && z.getLongitude() != null && z.getRadiusMeters() != null)
                .anyMatch(z -> meters(lat, lng, z) <= z.getRadiusMeters().doubleValue());
    }

    private static double meters(double lat, double lng, Zone z) {
        double dLat = Math.toRadians(z.getLatitude().doubleValue() - lat);
        double dLng = Math.toRadians(z.getLongitude().doubleValue() - lng);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat))
                * Math.cos(Math.toRadians(z.getLatitude().doubleValue())) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_M * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }
}
