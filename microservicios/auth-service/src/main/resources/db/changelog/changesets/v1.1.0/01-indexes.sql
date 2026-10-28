--liquibase formatted sql

-- Reemplaza la sección de índices de db/phase1_improvements.sql.
-- Cambios respecto al script original:
--   * Se indexan TODAS las llaves foráneas (había 25 sin índice).
--   * Índices compuestos (dueño + fecha) para las consultas "lo mío, lo más reciente primero".
--   * Índices parciales en vez de índices sobre booleanos/estados de baja cardinalidad.
--   * Se omiten users(email) y users(telephone): ya tienen índice UNIQUE.

--changeset alerta-mujer-team:v1.1.0-indexes-identity
CREATE INDEX IF NOT EXISTS idx_users_role_id ON identity.users(role_id);
CREATE INDEX IF NOT EXISTS idx_recovery_request_account_id ON identity.recovery_request(account_id);
CREATE INDEX IF NOT EXISTS idx_device_account_id ON identity.device(account_id);
CREATE INDEX IF NOT EXISTS idx_device_session_account_id ON identity.device_session(account_id);
CREATE INDEX IF NOT EXISTS idx_device_session_device_id ON identity.device_session(device_id);
CREATE INDEX IF NOT EXISTS idx_device_session_expires_at ON identity.device_session(expires_at);
--rollback DROP INDEX IF EXISTS identity.idx_users_role_id, identity.idx_recovery_request_account_id, identity.idx_device_account_id, identity.idx_device_session_account_id, identity.idx_device_session_device_id, identity.idx_device_session_expires_at;

--changeset alerta-mujer-team:v1.1.0-indexes-alert
CREATE INDEX IF NOT EXISTS idx_emergency_contact_user_profile_id ON alert.emergency_contact(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_alert_user_profile_created ON alert.alert(user_profile_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_alert_device_id ON alert.alert(device_id);
-- Sustituye a la vista materializada alert.active_alerts de la fase 3: siempre al día y sin copiar datos personales.
CREATE INDEX IF NOT EXISTS idx_alert_active ON alert.alert(started_at DESC) WHERE status = 'active';
CREATE INDEX IF NOT EXISTS idx_alert_contact_emergency_contact_id ON alert.alert_contact(emergency_contact_id);
CREATE INDEX IF NOT EXISTS idx_location_log_user_profile_recorded ON alert.location_log(user_profile_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_location_log_alert_id ON alert.location_log(alert_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_profile_created ON alert.notification(user_profile_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_unread ON alert.notification(user_profile_id) WHERE is_read = false;
CREATE INDEX IF NOT EXISTS idx_notification_alert_id ON alert.notification(alert_id);
CREATE INDEX IF NOT EXISTS idx_evidence_alert_id ON alert.evidence(alert_id);
CREATE INDEX IF NOT EXISTS idx_frequent_location_user_profile_id ON alert.frequent_location(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_alert_reminder_alert_id ON alert.alert_reminder(alert_id);
CREATE INDEX IF NOT EXISTS idx_alert_reminder_pending ON alert.alert_reminder(scheduled_at) WHERE status = 'scheduled';
--rollback DROP INDEX IF EXISTS alert.idx_emergency_contact_user_profile_id, alert.idx_alert_user_profile_created, alert.idx_alert_device_id, alert.idx_alert_active, alert.idx_alert_contact_emergency_contact_id, alert.idx_location_log_user_profile_recorded, alert.idx_location_log_alert_id, alert.idx_notification_user_profile_created, alert.idx_notification_unread, alert.idx_notification_alert_id, alert.idx_evidence_alert_id, alert.idx_frequent_location_user_profile_id, alert.idx_alert_reminder_alert_id, alert.idx_alert_reminder_pending;

--changeset alerta-mujer-team:v1.1.0-indexes-resource
CREATE INDEX IF NOT EXISTS idx_zone_city_active ON resource.zone(city) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_zone_report_user_profile_id ON resource.zone_report(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_zone_report_status ON resource.zone_report(status);
CREATE INDEX IF NOT EXISTS idx_emergency_resource_city_active ON resource.emergency_resource(city) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_resource_call_emergency_resource_id ON resource.resource_call(emergency_resource_id);
CREATE INDEX IF NOT EXISTS idx_resource_call_user_profile_id ON resource.resource_call(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_resource_call_alert_id ON resource.resource_call(alert_id);
CREATE INDEX IF NOT EXISTS idx_resource_call_device_id ON resource.resource_call(device_id);
--rollback DROP INDEX IF EXISTS resource.idx_zone_city_active, resource.idx_zone_report_user_profile_id, resource.idx_zone_report_status, resource.idx_emergency_resource_city_active, resource.idx_resource_call_emergency_resource_id, resource.idx_resource_call_user_profile_id, resource.idx_resource_call_alert_id, resource.idx_resource_call_device_id;

--changeset alerta-mujer-team:v1.1.0-indexes-admin
CREATE INDEX IF NOT EXISTS idx_audit_log_account_id ON admin.audit_log(account_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON admin.audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON admin.audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_user_report_reported_user_id ON admin.user_report(reported_user_id);
CREATE INDEX IF NOT EXISTS idx_user_report_reporter_user_id ON admin.user_report(reporter_user_id);
CREATE INDEX IF NOT EXISTS idx_user_report_status_created ON admin.user_report(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_moderation_action_user_report_id ON admin.moderation_action(user_report_id);
--rollback DROP INDEX IF EXISTS admin.idx_audit_log_account_id, admin.idx_audit_log_created_at, admin.idx_audit_log_entity, admin.idx_user_report_reported_user_id, admin.idx_user_report_reporter_user_id, admin.idx_user_report_status_created, admin.idx_moderation_action_user_report_id;
