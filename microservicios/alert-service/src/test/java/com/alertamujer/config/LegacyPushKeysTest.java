package com.alertamujer.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyPushKeysTest {

    @Test
    void addsTheOldKeysAndTypeSoOutdatedPhonesStillRing() {
        Map<String, String> data = LegacyPushKeys.withLegacyKeys(
                Map.of("type", "SOS_ALERT", "alertId", "7", "name", "Ana", "code", "abc"));

        assertEquals("SOS_ALERT", data.get("type"));
        assertEquals("ALERTA_SOS", data.get("tipo"));
        assertEquals("7", data.get("alertaId"));
        assertEquals("Ana", data.get("nombre"));
        assertEquals("abc", data.get("codigo"));
        assertEquals(8, data.size());
    }

    @Test
    void leavesUnknownKeysAlone() {
        assertEquals(Map.of("other", "x"), LegacyPushKeys.withLegacyKeys(Map.of("other", "x")));
    }
}
