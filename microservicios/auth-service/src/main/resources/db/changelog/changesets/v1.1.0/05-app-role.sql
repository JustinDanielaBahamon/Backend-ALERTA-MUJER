--liquibase formatted sql

-- Reemplaza las secciones RBAC de resources/db/security/defense-in-depth.sql.
-- Cambios respecto al script original:
--   * La contraseña NO está en el código: llega como parámetro de Liquibase
--     (spring.liquibase.parameters.app_db_password <- variable APP_DB_PASSWORD).
--   * Sin DROP ROLE (fallaría en cuanto el rol tenga permisos) y re-ejecutable.
--   * alerta_app no puede modificar ni borrar el log de auditoría.
--   * Liquibase (dueño del esquema) sigue haciendo el DDL; el backend se conecta como alerta_app.

--changeset alerta-mujer-team:v1.1.0-app-role-create splitStatements:false
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'alerta_app') THEN
        CREATE ROLE alerta_app WITH LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
    END IF;
END $$;
--rollback DROP OWNED BY alerta_app; DROP ROLE IF EXISTS alerta_app;

--changeset alerta-mujer-team:v1.1.0-app-role-password runAlways:true
ALTER ROLE alerta_app WITH LOGIN PASSWORD '${app_db_password}';
--rollback empty

--changeset alerta-mujer-team:v1.1.0-app-role-grants splitStatements:false
DO $$
DECLARE
    s TEXT;
BEGIN
    EXECUTE format('REVOKE CONNECT, TEMPORARY ON DATABASE %I FROM PUBLIC', current_database());
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO alerta_app', current_database());

    FOREACH s IN ARRAY ARRAY['identity', 'alert', 'resource', 'admin'] LOOP
        EXECUTE format('GRANT USAGE ON SCHEMA %I TO alerta_app', s);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO alerta_app', s);
        EXECUTE format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %I TO alerta_app', s);
        -- Tablas creadas en el futuro por el usuario de Liquibase
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alerta_app', s);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT USAGE, SELECT ON SEQUENCES TO alerta_app', s);
    END LOOP;

    -- Auditoría: solo leer e insertar
    REVOKE UPDATE, DELETE, TRUNCATE ON admin.audit_log FROM alerta_app;
END $$;
--rollback REVOKE ALL ON ALL TABLES IN SCHEMA identity, alert, resource, admin FROM alerta_app;
--rollback REVOKE ALL ON ALL SEQUENCES IN SCHEMA identity, alert, resource, admin FROM alerta_app;
--rollback REVOKE USAGE ON SCHEMA identity, alert, resource, admin FROM alerta_app;
