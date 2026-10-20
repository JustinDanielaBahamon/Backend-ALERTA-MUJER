--liquibase formatted sql

-- Parámetros nuevos que edita la administración desde Configuración:
--   * max_emergency_contacts: cuántos contactos de emergencia puede tener cada usuaria
--     (5 por defecto, que es el máximo que ya tiene alguna usuaria).
--   * min_app_version: versión mínima de la app móvil; las anteriores deben actualizarse.
--     NULL = no se exige ninguna.

--changeset alerta-mujer-team:v1.6.0-system-config-limits
ALTER TABLE admin.system_configuration ADD COLUMN max_emergency_contacts INTEGER NOT NULL DEFAULT 5;
ALTER TABLE admin.system_configuration ADD CONSTRAINT chk_system_configuration_max_contacts
    CHECK (max_emergency_contacts BETWEEN 1 AND 20);
ALTER TABLE admin.system_configuration ADD COLUMN min_app_version VARCHAR(20);
ALTER TABLE admin.system_configuration ADD CONSTRAINT chk_system_configuration_min_app_version
    CHECK (min_app_version IS NULL OR min_app_version ~ '^\d+(\.\d+){0,2}$');
--rollback ALTER TABLE admin.system_configuration DROP CONSTRAINT IF EXISTS chk_system_configuration_min_app_version;
--rollback ALTER TABLE admin.system_configuration DROP COLUMN IF EXISTS min_app_version;
--rollback ALTER TABLE admin.system_configuration DROP CONSTRAINT IF EXISTS chk_system_configuration_max_contacts;
--rollback ALTER TABLE admin.system_configuration DROP COLUMN IF EXISTS max_emergency_contacts;
