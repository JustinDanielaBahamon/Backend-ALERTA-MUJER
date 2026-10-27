--liquibase formatted sql

-- The automatic protection (changeset 24) writes values the old CHECK constraints rejected, so ending a
-- protection or raising its alerts failed in the database:
--   safe_trip.status:        'ended' (window over, she arrived at a frequent place or tapped "Detener").
--   safe_trip.alert_reason:  'no_answer' (did not answer "¿Todo bien?" in a risk zone),
--                            'unconfirmed' (never confirmed "¿Llegaste bien?"),
--                            'duress' (stopped it with her duress PIN).
--   alert.activation_method: 'auto' (alert raised by the automatic protection).

--changeset alerta-mujer-team:v1.6.0-auto-protection-values
ALTER TABLE alert.safe_trip DROP CONSTRAINT IF EXISTS chk_safe_trip_status;
ALTER TABLE alert.safe_trip ADD CONSTRAINT chk_safe_trip_status
    CHECK (status IN ('active', 'arrived', 'cancelled', 'alerted', 'ended'));
ALTER TABLE alert.safe_trip DROP CONSTRAINT IF EXISTS chk_safe_trip_alert_reason;
ALTER TABLE alert.safe_trip ADD CONSTRAINT chk_safe_trip_alert_reason
    CHECK (alert_reason IN ('not_confirmed', 'signal_lost', 'no_answer', 'unconfirmed', 'duress'));
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method
    CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact', 'power_button', 'signal_lost', 'auto'));
--rollback ALTER TABLE alert.safe_trip DROP CONSTRAINT IF EXISTS chk_safe_trip_status;
--rollback ALTER TABLE alert.safe_trip ADD CONSTRAINT chk_safe_trip_status CHECK (status IN ('active', 'arrived', 'cancelled', 'alerted'));
--rollback ALTER TABLE alert.safe_trip DROP CONSTRAINT IF EXISTS chk_safe_trip_alert_reason;
--rollback ALTER TABLE alert.safe_trip ADD CONSTRAINT chk_safe_trip_alert_reason CHECK (alert_reason IN ('not_confirmed', 'signal_lost'));
--rollback ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
--rollback ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact', 'power_button', 'signal_lost'));
