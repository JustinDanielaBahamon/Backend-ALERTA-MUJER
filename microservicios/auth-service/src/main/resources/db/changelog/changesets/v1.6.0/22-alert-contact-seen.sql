--liquibase formatted sql

-- Historial de la alerta: por cada canal (correo, notificación) se guarda cuándo el contacto lo
-- recibió (delivered_at, ya existía) y cuándo abrió el seguimiento desde ese canal (seen_at).

--changeset alerta-mujer-team:v1.6.0-alert-contact-seen
ALTER TABLE alert.alert_contact ADD COLUMN seen_at TIMESTAMP;
--rollback ALTER TABLE alert.alert_contact DROP COLUMN seen_at;
