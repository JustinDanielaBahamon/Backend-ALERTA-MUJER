--liquibase formatted sql

-- Reemplaza los CHECK de db/phase1_improvements.sql y de resources/db/security/defense-in-depth.sql
-- y los ENUM de db/phase2_improvements.sql.
--
-- Por qué CHECK y no ENUM: las entidades JPA mapean estos campos como String; con un tipo ENUM
-- de PostgreSQL Hibernate falla al insertar ("column is of type zone_type but expression is of
-- type character varying"). Además un CHECK se puede ampliar con un DROP/ADD, mientras que a un
-- ENUM no se le pueden quitar valores.
--
-- Listas reconciliadas (los scripts originales se contradecían):
--   zone_type      phase1/phase2: risk,safe,help,policia,asistencia | defense: risk,safe   -> unión
--   alert_type     phase1: ...,custom | defense: ...,fire                                 -> unión
--   alert status   phase1: sin 'expired' | defense: con 'expired'                          -> unión
--   activation     phase1 + 'widget' y 'suspicious_movement' (los muestra el panel admin web)
--   frequent_location.risk_level: ningún script lo cubría; valores reales de la app (en español)
--   teléfono       el regex de phase1 rechazaba datos existentes como '+57 311 000 0001'

--changeset alerta-mujer-team:v1.1.0-checks-identity
ALTER TABLE identity.users ADD CONSTRAINT chk_users_email_format
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
ALTER TABLE identity.users ADD CONSTRAINT chk_users_telephone_format
    CHECK (telephone IS NULL OR telephone ~ '^\+?[0-9][0-9 -]{5,18}[0-9]$');
ALTER TABLE identity.account ADD CONSTRAINT chk_account_status
    CHECK (status IN ('active', 'inactive', 'blocked', 'pending'));
ALTER TABLE identity.device ADD CONSTRAINT chk_device_status
    CHECK (status IN ('active', 'inactive', 'blocked', 'lost'));
ALTER TABLE identity.device ADD CONSTRAINT chk_device_gps_status
    CHECK (gps_status IN ('active', 'inactive', 'unknown', 'denied'));
ALTER TABLE identity.device_session ADD CONSTRAINT chk_device_session_status
    CHECK (status IN ('active', 'expired', 'revoked'));
--rollback ALTER TABLE identity.users DROP CONSTRAINT IF EXISTS chk_users_email_format, DROP CONSTRAINT IF EXISTS chk_users_telephone_format;
--rollback ALTER TABLE identity.account DROP CONSTRAINT IF EXISTS chk_account_status;
--rollback ALTER TABLE identity.device DROP CONSTRAINT IF EXISTS chk_device_status, DROP CONSTRAINT IF EXISTS chk_device_gps_status;
--rollback ALTER TABLE identity.device_session DROP CONSTRAINT IF EXISTS chk_device_session_status;

--changeset alerta-mujer-team:v1.1.0-checks-alert
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_status
    CHECK (status IN ('active', 'resolved', 'cancelled', 'expired'));
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_type
    CHECK (alert_type IN ('main', 'medical', 'police', 'fire', 'custom'));
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_activation_method
    CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual', 'widget', 'suspicious_movement'));
ALTER TABLE alert.alert ADD CONSTRAINT chk_alert_coordinates
    CHECK ((latitude IS NULL OR latitude BETWEEN -90 AND 90) AND (longitude IS NULL OR longitude BETWEEN -180 AND 180));
ALTER TABLE alert.location_log ADD CONSTRAINT chk_location_log_coordinates
    CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180);
ALTER TABLE alert.location_log ADD CONSTRAINT chk_location_log_accuracy
    CHECK (accuracy IS NULL OR accuracy >= 0);
ALTER TABLE alert.frequent_location ADD CONSTRAINT chk_frequent_location_coordinates
    CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180);
ALTER TABLE alert.frequent_location ADD CONSTRAINT chk_frequent_location_risk_level
    CHECK (risk_level IN ('muy_segura', 'moderada', 'muy_insegura'));
ALTER TABLE alert.emergency_contact ADD CONSTRAINT chk_emergency_contact_telephone_format
    CHECK (telephone ~ '^\+?[0-9][0-9 -]{5,18}[0-9]$');
ALTER TABLE alert.notification ADD CONSTRAINT chk_notification_priority
    CHECK (priority IN ('low', 'normal', 'high', 'urgent'));
--rollback ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_status, DROP CONSTRAINT IF EXISTS chk_alert_type, DROP CONSTRAINT IF EXISTS chk_alert_activation_method, DROP CONSTRAINT IF EXISTS chk_alert_coordinates;
--rollback ALTER TABLE alert.location_log DROP CONSTRAINT IF EXISTS chk_location_log_coordinates, DROP CONSTRAINT IF EXISTS chk_location_log_accuracy;
--rollback ALTER TABLE alert.frequent_location DROP CONSTRAINT IF EXISTS chk_frequent_location_coordinates, DROP CONSTRAINT IF EXISTS chk_frequent_location_risk_level;
--rollback ALTER TABLE alert.emergency_contact DROP CONSTRAINT IF EXISTS chk_emergency_contact_telephone_format;
--rollback ALTER TABLE alert.notification DROP CONSTRAINT IF EXISTS chk_notification_priority;

--changeset alerta-mujer-team:v1.1.0-checks-resource
ALTER TABLE resource.zone ADD CONSTRAINT chk_zone_type
    CHECK (zone_type IN ('risk', 'safe', 'help', 'policia', 'asistencia'));
ALTER TABLE resource.zone ADD CONSTRAINT chk_zone_risk_level
    CHECK (risk_level IN ('low', 'medium', 'high'));
ALTER TABLE resource.zone ADD CONSTRAINT chk_zone_radius_positive
    CHECK (radius_meters > 0);
ALTER TABLE resource.zone ADD CONSTRAINT chk_zone_coordinates
    CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180);
ALTER TABLE resource.zone_report ADD CONSTRAINT chk_zone_report_status
    CHECK (status IN ('pending', 'approved', 'rejected'));
ALTER TABLE resource.emergency_resource ADD CONSTRAINT chk_emergency_resource_type
    CHECK (resource_type IN ('emergency_line', 'police_station', 'police_post', 'health_center', 'hospital', 'support_organization', 'fire_station', 'ambulance'));
--rollback ALTER TABLE resource.zone DROP CONSTRAINT IF EXISTS chk_zone_type, DROP CONSTRAINT IF EXISTS chk_zone_risk_level, DROP CONSTRAINT IF EXISTS chk_zone_radius_positive, DROP CONSTRAINT IF EXISTS chk_zone_coordinates;
--rollback ALTER TABLE resource.zone_report DROP CONSTRAINT IF EXISTS chk_zone_report_status;
--rollback ALTER TABLE resource.emergency_resource DROP CONSTRAINT IF EXISTS chk_emergency_resource_type;

--changeset alerta-mujer-team:v1.1.0-checks-admin
ALTER TABLE admin.user_report ADD CONSTRAINT chk_user_report_status
    CHECK (status IN ('pending', 'in_review', 'reviewed', 'resolved', 'approved', 'rejected'));
--rollback ALTER TABLE admin.user_report DROP CONSTRAINT IF EXISTS chk_user_report_status;
