--liquibase formatted sql

-- "Avísame si no llego" con señal de vida: durante el trayecto el celular envía su ubicación cada
-- pocos minutos. Si deja de llegar (celular apagado, sin señal, se lo quitaron) el servidor activa la
-- alerta antes de la hora de llegada, con la última ubicación recibida.
-- La alerta guarda además cuándo se reenvió el aviso porque ningún contacto abrió el seguimiento.

--changeset alerta-mujer-team:v1.6.0-safe-trip-signal
ALTER TABLE alert.safe_trip
    ADD COLUMN last_seen_at   TIMESTAMP,
    ADD COLUMN last_latitude  NUMERIC(9, 6),
    ADD COLUMN last_longitude NUMERIC(9, 6),
    ADD COLUMN alert_reason   VARCHAR(20)
        CONSTRAINT chk_safe_trip_alert_reason CHECK (alert_reason IN ('not_confirmed', 'signal_lost'));
ALTER TABLE alert.alert ADD COLUMN escalated_at TIMESTAMP;
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method
    CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact', 'power_button', 'signal_lost'));
--rollback ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
--rollback ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact', 'power_button'));
--rollback ALTER TABLE alert.alert DROP COLUMN escalated_at;
--rollback ALTER TABLE alert.safe_trip DROP COLUMN alert_reason, DROP COLUMN last_longitude, DROP COLUMN last_latitude, DROP COLUMN last_seen_at;
