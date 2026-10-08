-- ============================================
-- AlertaMujer - Defense in Depth Security Script
-- PostgreSQL 16 Compatible
-- ============================================
-- This script implements:
-- 1. Role-based access control (RBAC)
-- 2. Row-Level Security (RLS)
-- 3. Audit triggers
-- 4. Check constraints
-- ============================================

-- ============================================
-- SECTION 1: ROLE-BASED ACCESS CONTROL (RBAC)
-- ============================================

-- Drop roles if they exist (for re-running the script)
DROP ROLE IF EXISTS alerta_app;
DROP ROLE IF EXISTS alerta_admin;

-- Create application user (DML only - runtime user)
CREATE ROLE alerta_app WITH
    LOGIN
    NOINHERIT
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    PASSWORD 'CHANGE_ME_ALERTA_APP_PASSWORD';

-- Create admin user (DDL + DML - deployment user)
CREATE ROLE alerta_admin WITH
    LOGIN
    NOINHERIT
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    PASSWORD 'CHANGE_ME_ALERTA_ADMIN_PASSWORD';

-- ============================================
-- SECTION 2: GRANT PRIVILEGES
-- ============================================

-- Grant schema usage and DML privileges to alerta_app
GRANT USAGE ON SCHEMA identity TO alerta_app;
GRANT USAGE ON SCHEMA alert TO alerta_app;
GRANT USAGE ON SCHEMA resource TO alerta_app;
GRANT USAGE ON SCHEMA admin TO alerta_app;

-- Grant SELECT, INSERT, UPDATE, DELETE on all tables to alerta_app
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA identity TO alerta_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA alert TO alerta_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA resource TO alerta_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA admin TO alerta_app;

-- Grant usage on sequences to alerta_app (for auto-increment IDs)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA identity TO alerta_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA alert TO alerta_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA resource TO alerta_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA admin TO alerta_app;

-- Grant execute on functions to alerta_app
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA identity TO alerta_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA alert TO alerta_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA resource TO alerta_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA admin TO alerta_app;

-- Automatically grant privileges on future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA identity GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA alert GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA resource GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA admin GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alerta_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA identity GRANT USAGE, SELECT ON SEQUENCES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA alert GRANT USAGE, SELECT ON SEQUENCES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA resource GRANT USAGE, SELECT ON SEQUENCES TO alerta_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA admin GRANT USAGE, SELECT ON SEQUENCES TO alerta_app;

-- Grant full DDL + DML privileges to alerta_admin
GRANT ALL PRIVILEGES ON SCHEMA identity TO alerta_admin;
GRANT ALL PRIVILEGES ON SCHEMA alert TO alerta_admin;
GRANT ALL PRIVILEGES ON SCHEMA resource TO alerta_admin;
GRANT ALL PRIVILEGES ON SCHEMA admin TO alerta_admin;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA identity TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA alert TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA resource TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA admin TO alerta_admin;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA identity TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA alert TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA resource TO alerta_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA admin TO alerta_admin;

-- ============================================
-- SECTION 3: ROW-LEVEL SECURITY (RLS)
-- ============================================

-- Add user_id column to alert table if not exists (for RLS)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'alert'
        AND table_name = 'alert'
        AND column_name = 'user_id'
    ) THEN
        ALTER TABLE alert.alert ADD COLUMN user_id BIGINT;
        COMMENT ON COLUMN alert.alert.user_id IS 'User ID for Row-Level Security - redundant with user_profile_id but needed for RLS policies';
    END IF;
END $$;

-- Add user_id column to location_log table if not exists (for RLS)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'alert'
        AND table_name = 'location_log'
        AND column_name = 'user_id'
    ) THEN
        ALTER TABLE alert.location_log ADD COLUMN user_id BIGINT;
        COMMENT ON COLUMN alert.location_log.user_id IS 'User ID for Row-Level Security - redundant with user_profile_id but needed for RLS policies';
    END IF;
END $$;

-- Create function to set current user context
CREATE OR REPLACE FUNCTION alert.set_current_user_id(p_user_id BIGINT)
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('alert.current_user_id', p_user_id::TEXT, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create function to get current user context
CREATE OR REPLACE FUNCTION alert.get_current_user_id()
RETURNS BIGINT AS $$
BEGIN
    RETURN NULLIF(current_setting('alert.current_user_id', true), '')::BIGINT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable RLS on alert table
ALTER TABLE alert.alert ENABLE ROW LEVEL SECURITY;

-- Create RLS policy for alert table: Users can only see their own alerts
CREATE POLICY alert_user_select_policy ON alert.alert
    FOR SELECT
    USING (user_id = alert.get_current_user_id());

-- Create RLS policy for alert table: Users can only insert their own alerts
CREATE POLICY alert_user_insert_policy ON alert.alert
    FOR INSERT
    WITH CHECK (user_id = alert.get_current_user_id());

-- Create RLS policy for alert table: Users can only update their own alerts
CREATE POLICY alert_user_update_policy ON alert.alert
    FOR UPDATE
    USING (user_id = alert.get_current_user_id())
    WITH CHECK (user_id = alert.get_current_user_id());

-- Create RLS policy for alert table: Users can only delete their own alerts
CREATE POLICY alert_user_delete_policy ON alert.alert
    FOR DELETE
    USING (user_id = alert.get_current_user_id());

-- Enable RLS on location_log table
ALTER TABLE alert.location_log ENABLE ROW LEVEL SECURITY;

-- Create RLS policy for location_log table: Users can only see their own location logs
CREATE POLICY location_log_user_select_policy ON alert.location_log
    FOR SELECT
    USING (user_id = alert.get_current_user_id());

-- Create RLS policy for location_log table: Users can only insert their own location logs
CREATE POLICY location_log_user_insert_policy ON alert.location_log
    FOR INSERT
    WITH CHECK (user_id = alert.get_current_user_id());

-- Create RLS policy for location_log table: Users can only update their own location logs
CREATE POLICY location_log_user_update_policy ON alert.location_log
    FOR UPDATE
    USING (user_id = alert.get_current_user_id())
    WITH CHECK (user_id = alert.get_current_user_id());

-- Create RLS policy for location_log table: Users can only delete their own location logs
CREATE POLICY location_log_user_delete_policy ON alert.location_log
    FOR DELETE
    USING (user_id = alert.get_current_user_id());

-- ============================================
-- SECTION 4: AUDIT TRIGGERS
-- ============================================

-- Create audit log function for users table
CREATE OR REPLACE FUNCTION identity.audit_users_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        INSERT INTO admin.audit_log (
            account_id,
            action,
            entity_type,
            entity_id,
            description,
            ip_address,
            created_at
        ) VALUES (
            COALESCE(NEW.id, OLD.id),
            'UPDATE',
            'USER',
            COALESCE(NEW.id, OLD.id),
            'User record updated',
            inet_client_addr(),
            NOW()
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO admin.audit_log (
            account_id,
            action,
            entity_type,
            entity_id,
            description,
            ip_address,
            created_at
        ) VALUES (
            OLD.id,
            'DELETE',
            'USER',
            OLD.id,
            'User record deleted',
            inet_client_addr(),
            NOW()
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create audit log function for alert table
CREATE OR REPLACE FUNCTION alert.audit_alert_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        INSERT INTO admin.audit_log (
            account_id,
            action,
            entity_type,
            entity_id,
            description,
            ip_address,
            created_at
        ) VALUES (
            COALESCE(NEW.user_id, OLD.user_id),
            'UPDATE',
            'ALERT',
            COALESCE(NEW.id, OLD.id),
            'Alert record updated',
            inet_client_addr(),
            NOW()
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO admin.audit_log (
            account_id,
            action,
            entity_type,
            entity_id,
            description,
            ip_address,
            created_at
        ) VALUES (
            OLD.user_id,
            'DELETE',
            'ALERT',
            OLD.id,
            'Alert record deleted',
            inet_client_addr(),
            NOW()
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create triggers on users table
DROP TRIGGER IF EXISTS users_audit_trigger ON identity.users;
CREATE TRIGGER users_audit_trigger
    AFTER UPDATE OR DELETE ON identity.users
    FOR EACH ROW EXECUTE FUNCTION identity.audit_users_trigger();

-- Create triggers on alert table
DROP TRIGGER IF EXISTS alert_audit_trigger ON alert.alert;
CREATE TRIGGER alert_audit_trigger
    AFTER UPDATE OR DELETE ON alert.alert
    FOR EACH ROW EXECUTE FUNCTION alert.audit_alert_trigger();

-- ============================================
-- SECTION 5: CHECK CONSTRAINTS
-- ============================================

-- Add latitude constraint to location_log
ALTER TABLE alert.location_log
    ADD CONSTRAINT chk_location_log_latitude
    CHECK (latitude >= -90 AND latitude <= 90);

-- Add longitude constraint to location_log
ALTER TABLE alert.location_log
    ADD CONSTRAINT chk_location_log_longitude
    CHECK (longitude >= -180 AND longitude <= 180);

-- Add latitude constraint to alert table (if latitude column still exists)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'alert'
        AND table_name = 'alert'
        AND column_name = 'latitude'
    ) THEN
        ALTER TABLE alert.alert
            ADD CONSTRAINT chk_alert_latitude
            CHECK (latitude >= -90 AND latitude <= 90);
    END IF;
END $$;

-- Add longitude constraint to alert table (if longitude column still exists)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'alert'
        AND table_name = 'alert'
        AND column_name = 'longitude'
    ) THEN
        ALTER TABLE alert.alert
            ADD CONSTRAINT chk_alert_longitude
            CHECK (longitude >= -180 AND longitude <= 180);
    END IF;
END $$;

-- Add radius constraint to zone table (radius cannot be negative)
ALTER TABLE resource.zone
    ADD CONSTRAINT chk_zone_radius_positive
    CHECK (radius_meters > 0);

-- Add risk level constraint to zone table
ALTER TABLE resource.zone
    ADD CONSTRAINT chk_zone_risk_level
    CHECK (risk_level IN ('low', 'medium', 'high'));

-- Add zone type constraint to zone table
ALTER TABLE resource.zone
    ADD CONSTRAINT chk_zone_type
    CHECK (zone_type IN ('risk', 'safe'));

-- Add status constraint to users table
ALTER TABLE identity.account
    ADD CONSTRAINT chk_account_status
    CHECK (status IN ('active', 'inactive', 'blocked', 'pending'));

-- Add status constraint to alert table
ALTER TABLE alert.alert
    ADD CONSTRAINT chk_alert_status
    CHECK (status IN ('active', 'resolved', 'cancelled', 'expired'));

-- Add alert type constraint to alert table
ALTER TABLE alert.alert
    ADD CONSTRAINT chk_alert_type
    CHECK (alert_type IN ('main', 'medical', 'police', 'fire'));

-- Add gps status constraint to device table
ALTER TABLE identity.device
    ADD CONSTRAINT chk_device_gps_status
    CHECK (gps_status IN ('active', 'inactive', 'unknown', 'denied'));

-- Add device status constraint to device table
ALTER TABLE identity.device
    ADD CONSTRAINT chk_device_status
    CHECK (status IN ('active', 'inactive', 'blocked', 'lost'));

-- ============================================
-- SECTION 6: SECURITY NOTES
-- ============================================

-- IMPORTANT: Change default passwords before production deployment
-- The passwords in this script are placeholders and must be changed:
-- - alerta_app: CHANGE_ME_ALERTA_APP_PASSWORD
-- - alerta_admin: CHANGE_ME_ALERTA_ADMIN_PASSWORD

-- To change passwords:
-- ALTER ROLE alerta_app WITH PASSWORD 'new_secure_password';
-- ALTER ROLE alerta_admin WITH PASSWORD 'new_secure_password';

-- IMPORTANT: Configure application to set user context
-- The Spring Boot application must call:
-- SELECT alert.set_current_user_id(:userId);
-- Before executing queries on alert or location_log tables

-- This can be done in a service layer method or via a custom interceptor

-- ============================================
-- SECTION 7: GRANT EXECUTE ON SECURITY FUNCTIONS
-- ============================================

-- Grant execute on RLS functions to alerta_app
GRANT EXECUTE ON FUNCTION alert.set_current_user_id(BIGINT) TO alerta_app;
GRANT EXECUTE ON FUNCTION alert.get_current_user_id() TO alerta_app;

-- Grant execute on audit functions to alerta_app
GRANT EXECUTE ON FUNCTION identity.audit_users_trigger() TO alerta_app;
GRANT EXECUTE ON FUNCTION alert.audit_alert_trigger() TO alerta_app;

-- ============================================
-- END OF SCRIPT
-- ============================================

-- Verify roles
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole
FROM pg_roles
WHERE rolname IN ('alerta_app', 'alerta_admin')
ORDER BY rolname;

-- Verify RLS is enabled
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE schemaname IN ('alert')
AND tablename IN ('alert', 'location_log')
ORDER BY schemaname, tablename;

-- Verify policies
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE schemaname = 'alert'
ORDER BY tablename, policyname;

-- Verify constraints
SELECT n.nspname AS schema,
       c.relname AS table,
       con.conname AS constraint,
       pg_get_constraintdef(con.oid) AS definition
FROM pg_constraint con
JOIN pg_class c ON con.conrelid = c.oid
JOIN pg_namespace n ON c.relnamespace = n.oid
WHERE n.nspname IN ('alert', 'identity', 'resource')
AND con.contype = 'c'
ORDER BY n.nspname, c.relname, con.conname;
