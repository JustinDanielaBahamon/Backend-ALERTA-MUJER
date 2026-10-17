--liquibase formatted sql

-- Fotos de la usuaria y de sus contactos, guardadas en la base como imagen pequeña en data URI
-- (JPEG de ~400 px, unas decenas de KB): así no se pierden al refrescar ni al cambiar de celular.
-- profile_photo_url era VARCHAR(500), suficiente para un enlace pero no para la imagen.

--changeset alerta-mujer-team:v1.6.0-photos
ALTER TABLE identity.user_profile ALTER COLUMN profile_photo_url TYPE TEXT;
ALTER TABLE alert.emergency_contact ADD COLUMN photo TEXT;
--rollback ALTER TABLE alert.emergency_contact DROP COLUMN photo;
--rollback ALTER TABLE identity.user_profile ALTER COLUMN profile_photo_url TYPE VARCHAR(500);
