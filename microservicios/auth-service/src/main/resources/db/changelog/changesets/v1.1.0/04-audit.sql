--liquibase formatted sql

-- Reemplaza la sección de auditoría de resources/db/security/defense-in-depth.sql.
-- Errores corregidos del script original:
--   * El trigger de alert usaba NEW.user_id, que nunca se llena; como audit_log.account_id era
--     NOT NULL, CUALQUIER UPDATE de una alerta (cancelar, resolver) habría fallado.
--   * El trigger de users guardaba users.id como account_id (son ids distintos; viola la FK o
--     apunta a otra cuenta).
--   * inet_client_addr() devuelve la IP del contenedor del backend, no la del usuario.
-- Diseño nuevo:
--   * account_id = actor de la acción, si la aplicación lo informa con
--     SET LOCAL app.current_account_id = '<id>' (y app.client_ip para la IP). Si no, queda NULL.
--   * Solo se registran los NOMBRES de las columnas cambiadas, nunca los valores
--     (evita copiar hashes de contraseña, documentos o coordenadas al log).
--   * audit_log es de solo inserción: UPDATE, DELETE y TRUNCATE se bloquean.

--changeset alerta-mujer-team:v1.1.0-audit-log-nullable-actor
ALTER TABLE admin.audit_log ALTER COLUMN account_id DROP NOT NULL;
--rollback ALTER TABLE admin.audit_log ALTER COLUMN account_id SET NOT NULL;

--changeset alerta-mujer-team:v1.1.0-audit-log-immutable splitStatements:false
CREATE OR REPLACE FUNCTION admin.forbid_audit_log_change()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'admin.audit_log es de solo inserción (% bloqueado)', TG_OP
        USING ERRCODE = 'insufficient_privilege';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_log_no_update_delete
    BEFORE UPDATE OR DELETE ON admin.audit_log
    FOR EACH ROW EXECUTE FUNCTION admin.forbid_audit_log_change();

CREATE TRIGGER trg_audit_log_no_truncate
    BEFORE TRUNCATE ON admin.audit_log
    FOR EACH STATEMENT EXECUTE FUNCTION admin.forbid_audit_log_change();
--rollback DROP TRIGGER IF EXISTS trg_audit_log_no_update_delete ON admin.audit_log;
--rollback DROP TRIGGER IF EXISTS trg_audit_log_no_truncate ON admin.audit_log;
--rollback DROP FUNCTION IF EXISTS admin.forbid_audit_log_change();

--changeset alerta-mujer-team:v1.1.0-audit-row-change-function splitStatements:false
CREATE OR REPLACE FUNCTION admin.log_row_change()
RETURNS TRIGGER AS $$
DECLARE
    v_row     JSONB;
    v_changed TEXT;
BEGIN
    IF TG_OP = 'UPDATE' THEN
        SELECT string_agg(n.key, ', ' ORDER BY n.key) INTO v_changed
        FROM jsonb_each(to_jsonb(NEW)) n
        WHERE n.key <> 'updated_at'
          AND n.value IS DISTINCT FROM to_jsonb(OLD) -> n.key;
        IF v_changed IS NULL THEN
            RETURN NEW;  -- solo cambió updated_at: nada que auditar
        END IF;
        v_row := to_jsonb(NEW);
    ELSIF TG_OP = 'DELETE' THEN
        v_row := to_jsonb(OLD);
    ELSE
        v_row := to_jsonb(NEW);
    END IF;

    INSERT INTO admin.audit_log (account_id, action, entity_type, entity_id, description, ip_address, created_at)
    VALUES (
        NULLIF(current_setting('app.current_account_id', true), '')::BIGINT,
        TG_OP,
        TG_ARGV[0],
        (v_row ->> 'id')::INTEGER,
        left(CASE WHEN TG_OP = 'UPDATE' THEN 'Columnas modificadas: ' || v_changed
                  ELSE TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME END, 500),
        NULLIF(current_setting('app.client_ip', true), ''),
        now()
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
--rollback DROP FUNCTION IF EXISTS admin.log_row_change();

--changeset alerta-mujer-team:v1.1.0-audit-triggers
CREATE TRIGGER trg_audit_users AFTER UPDATE OR DELETE ON identity.users
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('USER');
CREATE TRIGGER trg_audit_account AFTER UPDATE OR DELETE ON identity.account
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('ACCOUNT');
CREATE TRIGGER trg_audit_admin_profile AFTER INSERT OR UPDATE OR DELETE ON identity.admin_profile
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('ADMIN_PROFILE');
CREATE TRIGGER trg_audit_role AFTER INSERT OR UPDATE OR DELETE ON identity.role
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('ROLE');
CREATE TRIGGER trg_audit_alert AFTER UPDATE OR DELETE ON alert.alert
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('ALERT');
CREATE TRIGGER trg_audit_emergency_contact AFTER UPDATE OR DELETE ON alert.emergency_contact
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('EMERGENCY_CONTACT');
CREATE TRIGGER trg_audit_system_configuration AFTER UPDATE OR DELETE ON admin.system_configuration
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('SYSTEM_CONFIGURATION');
--rollback DROP TRIGGER IF EXISTS trg_audit_users ON identity.users;
--rollback DROP TRIGGER IF EXISTS trg_audit_account ON identity.account;
--rollback DROP TRIGGER IF EXISTS trg_audit_admin_profile ON identity.admin_profile;
--rollback DROP TRIGGER IF EXISTS trg_audit_role ON identity.role;
--rollback DROP TRIGGER IF EXISTS trg_audit_alert ON alert.alert;
--rollback DROP TRIGGER IF EXISTS trg_audit_emergency_contact ON alert.emergency_contact;
--rollback DROP TRIGGER IF EXISTS trg_audit_system_configuration ON admin.system_configuration;
