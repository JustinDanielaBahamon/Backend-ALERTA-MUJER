--liquibase formatted sql

-- Las secuencias de identity.role y resource.zone quedaron por detrás de max(id) tras el seed,
-- así que el siguiente INSERT (p. ej. crear una zona desde el panel admin) fallaba con
-- "duplicate key value violates unique constraint zone_pkey". Se sincronizan todas.

--changeset alerta-mujer-team:v1.1.0-sync-sequences splitStatements:false
DO $$
DECLARE
    r  RECORD;
    mx BIGINT;
BEGIN
    FOR r IN
        SELECT n.nspname AS s, c.relname AS t, pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), 'id') AS seq
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_attribute a ON a.attrelid = c.oid AND a.attname = 'id'
        WHERE n.nspname IN ('identity', 'alert', 'resource', 'admin') AND c.relkind = 'r'
    LOOP
        CONTINUE WHEN r.seq IS NULL;
        EXECUTE format('SELECT max(id) FROM %I.%I', r.s, r.t) INTO mx;
        PERFORM setval(r.seq, COALESCE(mx, 1), mx IS NOT NULL);
    END LOOP;
END $$;
--rollback empty
