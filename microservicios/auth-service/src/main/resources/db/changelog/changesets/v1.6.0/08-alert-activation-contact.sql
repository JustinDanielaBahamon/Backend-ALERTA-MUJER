--liquibase formatted sql

-- La app móvil puede activar una alerta desde la pantalla de contactos (aviso a un solo contacto).
-- Se registra como 'contact' para distinguirla del botón de pánico en el historial y los informes.

--changeset alerta-mujer-team:v1.6.0-alert-activation-contact
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method
    CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement', 'contact'));
--rollback ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_activation_method;
--rollback ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement'));
