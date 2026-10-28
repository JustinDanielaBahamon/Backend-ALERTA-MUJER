--liquibase formatted sql

-- Reemplaza la sección updated_at de db/phase1_improvements.sql.
-- Cambios respecto al script original:
--   * La columna nace con DEFAULT now() (antes quedaba NULL al insertar).
--   * El trigger se crea en TODAS las tablas que tienen updated_at (el original olvidaba varias).
--   * Se excluyen admin.audit_log (inmutable, ver 04-audit.sql) y alert.location_log
--     (puntos GPS de solo inserción: un trigger por fila solo añade costo).

--changeset alerta-mujer-team:v1.1.0-updated-at-function splitStatements:false
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
--rollback DROP FUNCTION IF EXISTS public.set_updated_at();

--changeset alerta-mujer-team:v1.1.0-updated-at-columns-and-triggers splitStatements:false
DO $$
DECLARE
    t TEXT;
    tables TEXT[] := ARRAY[
        'identity.role', 'identity.users', 'identity.account', 'identity.user_profile',
        'identity.admin_profile', 'identity.recovery_request', 'identity.device',
        'identity.device_permission', 'identity.device_session', 'identity.alert_activation_setting',
        'identity.user_preference',
        'alert.emergency_contact', 'alert.alert', 'alert.alert_contact', 'alert.notification',
        'alert.evidence', 'alert.frequent_location', 'alert.alert_reminder',
        'resource.zone', 'resource.zone_report', 'resource.emergency_resource', 'resource.resource_call',
        'admin.user_report', 'admin.moderation_action', 'admin.system_configuration'
    ];
BEGIN
    FOREACH t IN ARRAY tables LOOP
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT now()', t);
        EXECUTE format('ALTER TABLE %s ALTER COLUMN updated_at SET DEFAULT now()', t);
        EXECUTE format('DROP TRIGGER IF EXISTS trg_set_updated_at ON %s', t);
        EXECUTE format('CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON %s FOR EACH ROW EXECUTE FUNCTION public.set_updated_at()', t);
    END LOOP;
END $$;
--rollback DO $$ DECLARE r RECORD; BEGIN FOR r IN SELECT tgrelid::regclass AS tbl FROM pg_trigger WHERE tgname = 'trg_set_updated_at' LOOP EXECUTE format('DROP TRIGGER trg_set_updated_at ON %s', r.tbl); END LOOP; END $$;
