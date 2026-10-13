-- ============================================
-- FASE 2: Blindaje de Seguridad y Privacidad
-- AlertaMujer - PostgreSQL 16
-- ============================================
-- Este script debe ejecutarse DESPUÉS de phase1_improvements.sql
-- Ejecutar: psql -U postgres -d alertamujer -f phase2_improvements.sql
-- ============================================

-- ============================================
-- 1. ENUM TYPES (Tipos Enumerados)
-- Reemplaza validaciones de texto plano por tipos nativos
-- Mejora rendimiento y evita errores tipográficos
-- ============================================

-- Tipo para estado de cuenta
CREATE TYPE account_status AS ENUM ('active', 'inactive', 'blocked', 'pending');

-- Tipo para estado de alerta
CREATE TYPE alert_status AS ENUM ('active', 'resolved', 'cancelled');

-- Tipo para tipo de zona
CREATE TYPE zone_type AS ENUM ('risk', 'safe', 'help', 'policia', 'asistencia');

-- Tipo para nivel de riesgo
CREATE TYPE risk_level AS ENUM ('low', 'medium', 'high');

-- Tipo para tipo de alerta
CREATE TYPE alert_type AS ENUM ('main', 'medical', 'police', 'custom');

-- Tipo para método de activación
CREATE TYPE activation_method AS ENUM ('panic_button', 'gesture', 'voice', 'timer', 'manual');

-- Tipo para prioridad de notificación
CREATE TYPE notification_priority AS ENUM ('low', 'normal', 'high', 'urgent');

-- Tipo para estado de dispositivo
CREATE TYPE device_status AS ENUM ('active', 'inactive', 'blocked', 'lost');

-- Tipo para estado de GPS
CREATE TYPE gps_status AS ENUM ('active', 'inactive', 'unknown', 'denied');

-- Tipo para estado de sesión
CREATE TYPE session_status AS ENUM ('active', 'expired', 'revoked');

-- Tipo para estado de reporte
CREATE TYPE report_status AS ENUM ('pending', 'approved', 'rejected', 'reviewed');

-- Tipo para tipo de recurso de emergencia
CREATE TYPE resource_type AS ENUM ('emergency_line', 'police_station', 'police_post', 'health_center', 'hospital', 'support_organization', 'fire_station', 'ambulance');

-- ============================================
-- MIGRACIÓN DE COLUMNAS VARCHAR A ENUM
-- ============================================

-- identity.account: status
ALTER TABLE identity.account ALTER COLUMN status TYPE account_status USING status::account_status;

-- alert.alert: status
ALTER TABLE alert.alert ALTER COLUMN status TYPE alert_status USING status::alert_status;

-- alert.alert: alert_type
ALTER TABLE alert.alert ALTER COLUMN alert_type TYPE alert_type USING alert_type::alert_type;

-- alert.alert: activation_method
ALTER TABLE alert.alert ALTER COLUMN activation_method TYPE activation_method USING activation_method::activation_method;

-- alert.notification: priority
ALTER TABLE alert.notification ALTER COLUMN priority TYPE notification_priority USING priority::notification_priority;

-- resource.zone: zone_type
ALTER TABLE resource.zone ALTER COLUMN zone_type TYPE zone_type USING zone_type::zone_type;

-- resource.zone: risk_level
ALTER TABLE resource.zone ALTER COLUMN risk_level TYPE risk_level USING risk_level::risk_level;

-- resource.emergency_resource: resource_type
ALTER TABLE resource.emergency_resource ALTER COLUMN resource_type TYPE resource_type USING resource_type::resource_type;

-- identity.device: status
ALTER TABLE identity.device ALTER COLUMN status TYPE device_status USING status::device_status;

-- identity.device: gps_status
ALTER TABLE identity.device ALTER COLUMN gps_status TYPE gps_status USING gps_status::gps_status;

-- identity.device_session: status
ALTER TABLE identity.device_session ALTER COLUMN status TYPE session_status USING status::session_status;

-- admin.user_report: status
ALTER TABLE admin.user_report ALTER COLUMN status TYPE report_status USING status::report_status;

-- ============================================
-- ELIMINAR CHECK CONSTRAINTS ANTIGUOS (ahora son ENUM)
-- ============================================

ALTER TABLE identity.account DROP CONSTRAINT IF EXISTS chk_account_status;
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_status;
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_alert_type;
ALTER TABLE alert.alert DROP CONSTRAINT IF EXISTS chk_activation_method;
ALTER TABLE alert.notification DROP CONSTRAINT IF EXISTS chk_notification_priority;
ALTER TABLE resource.zone DROP CONSTRAINT IF EXISTS chk_zone_type;
ALTER TABLE resource.zone DROP CONSTRAINT IF EXISTS chk_risk_level;
ALTER TABLE identity.device DROP CONSTRAINT IF EXISTS chk_device_status;
ALTER TABLE identity.device DROP CONSTRAINT IF EXISTS chk_gps_status;

-- ============================================
-- 2. ENCRIPTACIÓN DE DATOS SENSIBLES
-- Usando pgcrypto (extensión de PostgreSQL)
-- ============================================

-- Habilitar extensión pgcrypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Función para encriptar datos
CREATE OR REPLACE FUNCTION encrypt_sensitive_data(data TEXT, key TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN encode(encrypt(data::bytea, digest(key, 'sha256'), 'aes'), 'base64');
END;
$$ LANGUAGE plpgsql;

-- Función para desencriptar datos
CREATE OR REPLACE FUNCTION decrypt_sensitive_data(encrypted_data TEXT, key TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN convert_from(decrypt(decode(encrypted_data, 'base64'), digest(key, 'sha256'), 'aes'), 'SQL_ASCII');
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- NOTA: La encriptación real requiere:
-- 1. Definir una clave de encriptación segura (almacenada en variable de entorno)
-- 2. Modificar la aplicación para usar las funciones de encriptación/desencriptación
-- 3. Migrar datos existentes a formato encriptado
-- ============================================

-- Ejemplo de cómo encriptar datos existentes (NO EJECUTAR SIN CLAVE SEGURA)
-- UPDATE identity.users SET telephone = encrypt_sensitive_data(telephone, 'TU_CLAVE_SECRETA') WHERE telephone IS NOT NULL;
-- UPDATE identity.users SET document_number = encrypt_sensitive_data(document_number, 'TU_CLAVE_SECRETA') WHERE document_number IS NOT NULL;
-- UPDATE identity.recovery_request SET token_hash = encrypt_sensitive_data(token_hash, 'TU_CLAVE_SECRETA') WHERE token_hash IS NOT NULL;

-- ============================================
-- VISTA PARA ACCESO SEGURO A DATOS SENSIBLES
-- Esta vista desencripta datos en tiempo real (requiere clave)
-- ============================================

-- Crear vista para usuarios con datos desencriptados (solo para administradores)
-- CREATE OR REPLACE VIEW identity.users_decrypted AS
-- SELECT 
--     id,
--     role_id,
--     first_name,
--     last_name,
--     decrypt_sensitive_data(telephone, current_setting('app.encryption_key')) AS telephone,
--     email,
--     decrypt_sensitive_data(document_number, current_setting('app.encryption_key')) AS document_number,
--     document_type,
--     birthdate,
--     created_at,
--     updated_at
-- FROM identity.users;

-- ============================================
-- POLÍTICAS DE SEGURIDAD ADICIONALES
-- ============================================

-- Restringir acceso a datos sensibles (requiere rol de admin)
-- REVOKE SELECT ON identity.users FROM public;
-- GRANT SELECT ON identity.users TO alertamujer_admin;
-- GRANT SELECT (id, role_id, first_name, last_name, email, document_type, birthdate, created_at, updated_at) ON identity.users TO alertamujer_app;

-- ============================================
-- FIN DE FASE 2
-- ============================================
-- Verificar que todo se aplicó correctamente:
-- SELECT typname FROM pg_type WHERE typtype = 'e';
-- SELECT proname FROM pg_proc WHERE proname LIKE '%encrypt%' OR proname LIKE '%decrypt%';
-- ============================================

-- ============================================
-- INSTRUCCIONES PARA ENCRIPTACIÓN REAL
-- ============================================
-- 1. Definir una clave de encriptación segura (32+ caracteres)
-- 2. Almacenar la clave en variable de entorno: SET app.encryption_key = 'tu_clave';
-- 3. Modificar la aplicación Spring Boot para usar las funciones de encriptación
-- 4. Crear un script de migración para encriptar datos existentes
-- 5. Probar el proceso de encriptación/desencriptación en ambiente de desarrollo
-- 6. Documentar el proceso de recuperación en caso de pérdida de clave
-- ============================================
