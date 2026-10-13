-- ============================================
-- FASE 1: Correcciones Críticas de Rendimiento e Integridad
-- AlertaMujer - PostgreSQL 16
-- ============================================
-- Este script debe ejecutarse DESPUÉS de init.sql
-- Ejecutar: psql -U postgres -d alertamujer -f phase1_improvements.sql
-- ============================================

-- ============================================
-- 1. ÍNDICES FALTANTES (CRÍTICO)
-- Mejora significativa en consultas frecuentes
-- ============================================

-- ALERT SCHEMA: location_log
-- Consultas por usuario, alerta y fecha
CREATE INDEX IF NOT EXISTS idx_location_log_user_profile_id ON alert.location_log(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_location_log_alert_id ON alert.location_log(alert_id);
CREATE INDEX IF NOT EXISTS idx_location_log_recorded_at ON alert.location_log(recorded_at DESC);

-- ALERT SCHEMA: notification
-- Consultas por usuario, alerta, estado y fecha
CREATE INDEX IF NOT EXISTS idx_notification_user_profile_id ON alert.notification(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_notification_alert_id ON alert.notification(alert_id);
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON alert.notification(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON alert.notification(created_at DESC);

-- ALERT SCHEMA: frequent_location
-- Consultas por usuario y estado activo
CREATE INDEX IF NOT EXISTS idx_frequent_location_user_profile_id ON alert.frequent_location(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_frequent_location_is_active ON alert.frequent_location(is_active);

-- ALERT SCHEMA: alert
-- Consultas por usuario, estado y fecha
CREATE INDEX IF NOT EXISTS idx_alert_user_profile_id ON alert.alert(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_alert_status ON alert.alert(status);
CREATE INDEX IF NOT EXISTS idx_alert_created_at ON alert.alert(created_at DESC);

-- RESOURCE SCHEMA: zone
-- Consultas por ciudad, tipo y estado activo
CREATE INDEX IF NOT EXISTS idx_zone_city ON resource.zone(city);
CREATE INDEX IF NOT EXISTS idx_zone_zone_type ON resource.zone(zone_type);
CREATE INDEX IF NOT EXISTS idx_zone_is_active ON resource.zone(is_active);

-- RESOURCE SCHEMA: emergency_resource
-- Consultas por ciudad, tipo y estado activo
CREATE INDEX IF NOT EXISTS idx_emergency_resource_city ON resource.emergency_resource(city);
CREATE INDEX IF NOT EXISTS idx_emergency_resource_resource_type ON resource.emergency_resource(resource_type);
CREATE INDEX IF NOT EXISTS idx_emergency_resource_is_active ON resource.emergency_resource(is_active);

-- ADMIN SCHEMA: audit_log
-- Consultas por cuenta, entidad y fecha
CREATE INDEX IF NOT EXISTS idx_audit_log_account_id ON admin.audit_log(account_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity_type ON admin.audit_log(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON admin.audit_log(created_at DESC);

-- ADMIN SCHEMA: user_report
-- Consultas por estado y fecha
CREATE INDEX IF NOT EXISTS idx_user_report_status ON admin.user_report(status);
CREATE INDEX IF NOT EXISTS idx_user_report_created_at ON admin.user_report(created_at DESC);

-- IDENTITY SCHEMA: users
-- Consultas por email y teléfono
CREATE INDEX IF NOT EXISTS idx_users_email ON identity.users(email);
CREATE INDEX IF NOT EXISTS idx_users_telephone ON identity.users(telephone);

-- IDENTITY SCHEMA: device
-- Consultas por cuenta y estado
CREATE INDEX IF NOT EXISTS idx_device_account_id ON identity.device(account_id);
CREATE INDEX IF NOT EXISTS idx_device_status ON identity.device(status);

-- IDENTITY SCHEMA: device_session
-- Consultas por cuenta y expiración
CREATE INDEX IF NOT EXISTS idx_device_session_account_id ON identity.device_session(account_id);
CREATE INDEX IF NOT EXISTS idx_device_session_expires_at ON identity.device_session(expires_at);

-- ============================================
-- 2. CHECK CONSTRAINTS (VALIDACIÓN DE FORMATOS)
-- Evita datos corruptos desde las apps móviles
-- ============================================

-- Validar formato de email en identity.users
ALTER TABLE identity.users 
ADD CONSTRAINT chk_email_format 
CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Validar formato de teléfono internacional en identity.users
-- Permite: +57 311 000 0001, +573110000001, 3110000001
ALTER TABLE identity.users 
ADD CONSTRAINT chk_telephone_format 
CHECK (telephone IS NULL OR telephone ~* '^(\+[0-9]{1,3}[\s-]?[0-9]{9,15}|[0-9]{9,15})$');

-- Validar que status de account tenga valores válidos
ALTER TABLE identity.account 
ADD CONSTRAINT chk_account_status 
CHECK (status IN ('active', 'inactive', 'blocked', 'pending'));

-- Validar que status de alert tenga valores válidos
ALTER TABLE alert.alert 
ADD CONSTRAINT chk_alert_status 
CHECK (status IN ('active', 'resolved', 'cancelled'));

-- Validar que zone_type tenga valores válidos
ALTER TABLE resource.zone 
ADD CONSTRAINT chk_zone_type 
CHECK (zone_type IN ('risk', 'safe', 'help', 'policia', 'asistencia'));

-- Validar que risk_level tenga valores válidos
ALTER TABLE resource.zone 
ADD CONSTRAINT chk_risk_level 
CHECK (risk_level IN ('low', 'medium', 'high'));

-- Validar que alert_type tenga valores válidos
ALTER TABLE alert.alert 
ADD CONSTRAINT chk_alert_type 
CHECK (alert_type IN ('main', 'medical', 'police', 'custom'));

-- Validar que activation_method tenga valores válidos
ALTER TABLE alert.alert 
ADD CONSTRAINT chk_activation_method 
CHECK (activation_method IN ('panic_button', 'gesture', 'voice', 'timer', 'manual'));

-- Validar que notification priority tenga valores válidos
ALTER TABLE alert.notification 
ADD CONSTRAINT chk_notification_priority 
CHECK (priority IN ('low', 'normal', 'high', 'urgent'));

-- Validar que device status tenga valores válidos
ALTER TABLE identity.device 
ADD CONSTRAINT chk_device_status 
CHECK (status IN ('active', 'inactive', 'blocked', 'lost'));

-- Validar que gps_status tenga valores válidos
ALTER TABLE identity.device 
ADD CONSTRAINT chk_gps_status 
CHECK (gps_status IN ('active', 'inactive', 'unknown', 'denied'));

-- ============================================
-- 3. UPDATED_AT GLOBAL CON TRIGGERS
-- Actualización automática de timestamp en todas las tablas
-- ============================================

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Agregar updated_at a tablas que no lo tienen
ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.account ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.admin_profile ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.recovery_request ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.device ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.device_session ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE identity.alert_activation_setting ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.emergency_contact ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.alert ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.alert_contact ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.location_log ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.notification ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.evidence ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE alert.alert_reminder ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE resource.zone_report ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE resource.emergency_resource ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE resource.resource_call ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE admin.audit_log ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE admin.user_report ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE admin.moderation_action ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Crear triggers para actualizar updated automáticamente
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON identity.users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_account_updated_at
    BEFORE UPDATE ON identity.account
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_user_profile_updated_at
    BEFORE UPDATE ON identity.user_profile
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_admin_profile_updated_at
    BEFORE UPDATE ON identity.admin_profile
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_recovery_request_updated_at
    BEFORE UPDATE ON identity.recovery_request
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_device_updated_at
    BEFORE UPDATE ON identity.device
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_device_session_updated_at
    BEFORE UPDATE ON identity.device_session
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_alert_activation_setting_updated_at
    BEFORE UPDATE ON identity.alert_activation_setting
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_emergency_contact_updated_at
    BEFORE UPDATE ON alert.emergency_contact
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_alert_updated_at
    BEFORE UPDATE ON alert.alert
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_alert_contact_updated_at
    BEFORE UPDATE ON alert.alert_contact
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_location_log_updated_at
    BEFORE UPDATE ON alert.location_log
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_notification_updated_at
    BEFORE UPDATE ON alert.notification
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_evidence_updated_at
    BEFORE UPDATE ON alert.evidence
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_alert_reminder_updated_at
    BEFORE UPDATE ON alert.alert_reminder
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_zone_report_updated_at
    BEFORE UPDATE ON resource.zone_report
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_emergency_resource_updated_at
    BEFORE UPDATE ON resource.emergency_resource
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_resource_call_updated_at
    BEFORE UPDATE ON resource.resource_call
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_audit_log_updated_at
    BEFORE UPDATE ON admin.audit_log
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_user_report_updated_at
    BEFORE UPDATE ON admin.user_report
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_moderation_action_updated_at
    BEFORE UPDATE ON admin.moderation_action
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- FIN DE FASE 1
-- ============================================
-- Verificar que todo se aplicó correctamente:
-- SELECT indexname FROM pg_indexes WHERE schemaname IN ('identity', 'alert', 'resource', 'admin');
-- SELECT conname FROM pg_constraint WHERE conname LIKE 'chk_%';
-- SELECT tgname FROM pg_trigger WHERE tgname LIKE 'trigger_%';
-- ============================================
