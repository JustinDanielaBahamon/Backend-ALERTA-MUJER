--liquibase formatted sql

-- Constancia de la aceptación de los términos y la política de privacidad: cuándo y qué versión.
-- Las cuentas anteriores quedan en null (no hay registro de cuándo aceptaron).

--changeset alerta-mujer-team:v1.6.0-terms-acceptance
ALTER TABLE identity.users ADD COLUMN terms_accepted_at TIMESTAMP;
ALTER TABLE identity.users ADD COLUMN terms_version VARCHAR(20);
--rollback ALTER TABLE identity.users DROP COLUMN terms_version;
--rollback ALTER TABLE identity.users DROP COLUMN terms_accepted_at;
