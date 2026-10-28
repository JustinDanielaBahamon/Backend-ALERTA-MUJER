--liquibase formatted sql

-- La app permite activar la alerta con 4 pulsaciones rápidas del botón de encendido (también con la
-- pantalla apagada). Se registra como 'power_button' en el historial y los informes.

--changeset alerta-mujer-team:v1.6.0-alert-activation-power-button
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method
    CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact', 'power_button'));
--rollback ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
--rollback ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact'));
