package com.alertamujer.shared.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Dates are stored as LocalDateTime in the server zone (UTC in Docker and Render).
 * They used to go out without a zone ("2026-10-16T12:42:25") and the web and mobile took them as local
 * time, showing 5 extra hours in Colombia. Now they go out with their offset ("2026-10-16T12:42:25Z")
 * and each client shows them in the device time. On input both forms are accepted: with an offset
 * they are converted to the server zone; without one they are taken as is (as before).
 */
@Configuration
public class JsonTimeConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer datesWithZone() {
        return builder -> builder
                .serializerByType(LocalDateTime.class, new WithZone())
                .deserializerByType(LocalDateTime.class, new FlexibleLocalDateTime());
    }

    static final class WithZone extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
    }

    static final class FlexibleLocalDateTime extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isBlank()) return null;
            String t = text.trim();
            boolean hasOffset = t.endsWith("Z") || t.matches(".*[+-]\\d{2}:?\\d{2}$");
            return hasOffset
                    ? OffsetDateTime.parse(t).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.parse(t);
        }
    }
}
