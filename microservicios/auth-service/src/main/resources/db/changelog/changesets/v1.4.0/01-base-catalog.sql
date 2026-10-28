--liquibase formatted sql

-- Datos mínimos para que el sistema funcione en cualquier ambiente (también producción).
-- Los datos de ejemplo de 05-seed-data.yaml solo corren con el contexto dev/test porque
-- incluyen cuentas de prueba con contraseñas conocidas, que no deben existir en un servidor
-- público. Sin los roles, el registro de usuarias falla. En dev/test no hace nada porque
-- las filas ya existen.

--changeset alerta-mujer-team:v1.4.0-base-roles
INSERT INTO identity.role (id, name, description) VALUES
    (1, 'user', 'Usuario estándar de la aplicación'),
    (2, 'administrator', 'Administrador del sistema')
ON CONFLICT (id) DO NOTHING;
--rollback empty

--changeset alerta-mujer-team:v1.4.0-base-system-configuration
INSERT INTO admin.system_configuration (singleton_key, sos_max_duration_minutes, emergency_phone_number, admin_email,
                                        notifications_enabled, default_notification_priority, default_theme)
SELECT 'SYSTEM', 30, '123', 'admin@alertamujer.local', TRUE, 'normal', 'light'
WHERE NOT EXISTS (SELECT 1 FROM admin.system_configuration WHERE singleton_key = 'SYSTEM');
--rollback empty
