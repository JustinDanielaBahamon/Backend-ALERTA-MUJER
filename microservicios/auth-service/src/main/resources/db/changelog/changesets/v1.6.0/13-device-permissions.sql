--liquibase formatted sql

-- Permisos del celular que informa la app (ubicación, notificaciones, agitar, micrófono...), para
-- que la usuaria vea en la web si su teléfono está listo para una emergencia.

--changeset alerta-mujer-team:v1.6.0-device-permissions
ALTER TABLE identity.device ADD COLUMN permissions VARCHAR(400);
ALTER TABLE identity.device ADD COLUMN permissions_updated_at TIMESTAMP;
--rollback ALTER TABLE identity.device DROP COLUMN permissions_updated_at;
--rollback ALTER TABLE identity.device DROP COLUMN permissions;
