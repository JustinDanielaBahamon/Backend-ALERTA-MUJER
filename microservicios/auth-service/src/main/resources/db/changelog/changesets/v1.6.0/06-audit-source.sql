--liquibase formatted sql

-- Origen de cada registro de auditoría: desde dónde se hizo el cambio
-- ("Panel web · Chrome · Linux", "App móvil · Android", "Tarea automática · …").
-- La aplicación lo informa con SET LOCAL app.source, igual que app.current_account_id.
-- Si queda NULL, el cambio no pasó por la aplicación (p. ej. SQL directo) o es anterior a esto.

--changeset alerta-mujer-team:v1.6.0-audit-source-column
ALTER TABLE admin.audit_log ADD COLUMN source VARCHAR(120);
--rollback ALTER TABLE admin.audit_log DROP COLUMN IF EXISTS source;

--changeset alerta-mujer-team:v1.6.0-audit-source-row-change splitStatements:false
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

    INSERT INTO admin.audit_log (account_id, action, entity_type, entity_id, description, ip_address, source, created_at)
    VALUES (
        NULLIF(current_setting('app.current_account_id', true), '')::BIGINT,
        TG_OP,
        TG_ARGV[0],
        (v_row ->> 'id')::INTEGER,
        left(CASE WHEN TG_OP = 'UPDATE' THEN 'Columnas modificadas: ' || v_changed
                  ELSE TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME END, 500),
        NULLIF(current_setting('app.client_ip', true), ''),
        NULLIF(current_setting('app.source', true), ''),
        now()
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
--rollback SELECT 1;

--changeset alerta-mujer-team:v1.6.0-audit-source-config-change splitStatements:false
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

    INSERT INTO admin.audit_log (account_id, action, entity_type, entity_id, description, ip_address, source, created_at)
    VALUES (
        NULLIF(current_setting('app.current_account_id', true), '')::BIGINT,
        'UPDATE',
        'SYSTEM_CONFIGURATION',
        NEW.id::INTEGER,
        left('Valores: ' || v_changes, 500),
        NULLIF(current_setting('app.client_ip', true), ''),
        NULLIF(current_setting('app.source', true), ''),
        now()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
--rollback SELECT 1;
