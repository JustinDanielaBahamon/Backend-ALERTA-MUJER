--liquibase formatted sql

-- Las evidencias se borran a los 60 días; la usuaria puede marcar una alerta para conservarlas
-- (por ejemplo, si va a poner una denuncia).

--changeset alerta-mujer-team:v1.6.0-alert-keep-evidence
ALTER TABLE alert.alert ADD COLUMN keep_evidence BOOLEAN NOT NULL DEFAULT FALSE;
--rollback ALTER TABLE alert.alert DROP COLUMN keep_evidence;
