--liquibase formatted sql

-- Nombre que la usuaria le puso a su celular en Android (p. ej. "Galaxy de Ana"), para reconocerlo
-- en "Mis dispositivos" cuando tiene más de uno.

--changeset alerta-mujer-team:v1.6.0-device-name
ALTER TABLE identity.device ADD COLUMN device_name VARCHAR(100);
--rollback ALTER TABLE identity.device DROP COLUMN device_name;
