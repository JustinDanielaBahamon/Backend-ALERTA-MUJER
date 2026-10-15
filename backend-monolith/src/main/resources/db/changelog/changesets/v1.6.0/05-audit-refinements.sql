--liquibase formatted sql

-- Ajustes de auditoría para el módulo de Auditoría del panel:
--   * log_row_change deja de registrar las actualizaciones que solo cambian last_access
--     (cada inicio de sesión generaba un "Modificó cuenta"); los inicios de sesión se
--     registran ahora como eventos propios desde la aplicación (LOGIN_SUCCESS, etc.).
--   * Los parámetros del sistema guardan el valor anterior y el nuevo: no son datos
--     personales y sin ellos no se puede saber qué se cambió. El resto de tablas sigue
--     registrando solo nombres de columnas.

--changeset alerta-mujer-team:v1.6.0-audit-ignore-last-access splitStatements:false
CREATE OR REPLACE FUNCTION admin.log_row_change()
RETURNS TRIGGER AS $$
DECLARE
    v_row     JSONB;
    v_changed TEXT;
BEGIN
    IF TG_OP = 'UPDATE' THEN
        SELECT string_agg(n.key, ', ' ORDER BY n.key) INTO v_changed
        FROM jsonb_each(to_jsonb(NEW)) n
        WHERE n.key NOT IN ('updated_at', 'last_access')
          AND n.value IS DISTINCT FROM to_jsonb(OLD) -> n.key;
        IF v_changed IS NULL THEN
            RETURN NEW;  -- solo cambiaron marcas de tiempo: nada que auditar
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
--rollback SELECT 1;

--changeset alerta-mujer-team:v1.6.0-audit-config-values splitStatements:false
CREATE OR REPLACE FUNCTION admin.log_config_change()
RETURNS TRIGGER AS $$
DECLARE
    v_changes TEXT;
BEGIN
    SELECT string_agg(n.key || ': ' || COALESCE(to_jsonb(OLD) ->> n.key, '(vacío)')
                      || ' → ' || COALESCE(n.value #>> '{}', '(vacío)'), '; ' ORDER BY n.key)
      INTO v_changes
    FROM jsonb_each(to_jsonb(NEW)) n
    WHERE n.key NOT IN ('updated_at', 'updated_by_admin_id', 'created_at')
      AND n.value IS DISTINCT FROM to_jsonb(OLD) -> n.key;
    IF v_changes IS NULL THEN
        RETURN NEW;
    END IF;

    INSERT INTO admin.audit_log (account_id, action, entity_type, entity_id, description, ip_address, created_at)
    VALUES (
        NULLIF(current_setting('app.current_account_id', true), '')::BIGINT,
        'UPDATE',
        'SYSTEM_CONFIGURATION',
        NEW.id::INTEGER,
        left('Valores: ' || v_changes, 500),
        NULLIF(current_setting('app.client_ip', true), ''),
        now()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_audit_system_configuration ON admin.system_configuration;
CREATE TRIGGER trg_audit_system_configuration AFTER UPDATE ON admin.system_configuration
    FOR EACH ROW EXECUTE FUNCTION admin.log_config_change();
--rollback DROP TRIGGER IF EXISTS trg_audit_system_configuration ON admin.system_configuration;
--rollback CREATE TRIGGER trg_audit_system_configuration AFTER UPDATE OR DELETE ON admin.system_configuration FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('SYSTEM_CONFIGURATION');
--rollback DROP FUNCTION IF EXISTS admin.log_config_change();
