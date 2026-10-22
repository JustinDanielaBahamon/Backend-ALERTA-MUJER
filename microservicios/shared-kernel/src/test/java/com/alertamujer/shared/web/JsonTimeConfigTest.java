package com.alertamujer.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTimeConfigTest {

    private final ObjectMapper mapper = mapper();

    private static ObjectMapper mapper() {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
        new JsonTimeConfig().datesWithZone().customize(b);
        return b.build();
    }

    record Con(LocalDateTime t) {}

    @Test
    void datesAreWrittenWithTheirZone() throws Exception {
        LocalDateTime t = LocalDateTime.of(2026, 10, 16, 12, 42, 25);
        String json = mapper.writeValueAsString(Map.of("t", t));

        String expected = t.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
        assertTrue(json.contains(expected.substring(0, 19)), json);
        assertTrue(json.matches(".*(Z|[+-]\\d{2}:\\d{2})\".*"), json); // termina con su desfase
    }

    @Test
    void acceptsDatesWithAndWithoutZone() throws Exception {
        assertEquals(LocalDateTime.of(2026, 10, 11, 0, 0),
                mapper.readValue("{\"t\":\"2026-10-11T00:00:00\"}", Con.class).t());

        LocalDateTime withZone = mapper.readValue("{\"t\":\"2026-10-16T07:42:00-05:00\"}", Con.class).t();
        assertEquals(java.time.OffsetDateTime.parse("2026-10-16T07:42:00-05:00")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(), withZone);
    }
}
