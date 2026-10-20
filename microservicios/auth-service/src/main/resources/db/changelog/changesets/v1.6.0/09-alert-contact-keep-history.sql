--liquibase formatted sql

-- Una usuaria no podía borrar un contacto que ya había recibido una alerta: la llave foránea de
-- alert_contact lo impedía. Ahora el envío se conserva en el historial (canal, destino, estado)
-- y solo pierde la referencia al contacto borrado.

--changeset alerta-mujer-team:v1.6.0-alert-contact-keep-history
ALTER TABLE alert.alert_contact ALTER COLUMN emergency_contact_id DROP NOT NULL;
ALTER TABLE alert.alert_contact DROP CONSTRAINT fk_alert_contact_emergency_contact;
ALTER TABLE alert.alert_contact ADD CONSTRAINT fk_alert_contact_emergency_contact
    FOREIGN KEY (emergency_contact_id) REFERENCES alert.emergency_contact(id) ON DELETE SET NULL;
--rollback ALTER TABLE alert.alert_contact DROP CONSTRAINT fk_alert_contact_emergency_contact;
--rollback ALTER TABLE alert.alert_contact ADD CONSTRAINT fk_alert_contact_emergency_contact FOREIGN KEY (emergency_contact_id) REFERENCES alert.emergency_contact(id);
--rollback ALTER TABLE alert.alert_contact ALTER COLUMN emergency_contact_id SET NOT NULL;
