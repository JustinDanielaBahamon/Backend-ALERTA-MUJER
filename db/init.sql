-- ============================================
-- AlertaMujer - PostgreSQL 16 Schema + Seed
-- Based on SRS and team database design
-- ============================================

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS alert;
CREATE SCHEMA IF NOT EXISTS resource;
CREATE SCHEMA IF NOT EXISTS admin;

-- ============================================
-- IDENTITY SERVICE TABLES (order matters for FK)
-- ============================================

CREATE TABLE identity.role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE identity.users (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL DEFAULT 1 REFERENCES identity.role(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    telephone VARCHAR(20) UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    document_number VARCHAR(20) UNIQUE,
    document_type VARCHAR(20),
    birthdate DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE identity.account (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES identity.users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    last_access TIMESTAMP
);

CREATE TABLE identity.user_profile (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES identity.users(id) ON DELETE CASCADE,
    profile_photo_url VARCHAR(500),
    tutorial_completed BOOLEAN NOT NULL DEFAULT FALSE,
    tutorial_seen_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE identity.admin_profile (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL UNIQUE REFERENCES identity.account(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE identity.recovery_request (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES identity.account(id) ON DELETE CASCADE,
    method VARCHAR(20) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    requested_ip VARCHAR(45)
);

CREATE TABLE identity.device (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES identity.account(id) ON DELETE CASCADE,
    device_uuid VARCHAR(255) NOT NULL UNIQUE,
    brand VARCHAR(50),
    model VARCHAR(100),
    os_name VARCHAR(30) NOT NULL,
    os_version VARCHAR(30),
    app_version VARCHAR(30),
    gps_status VARCHAR(20) NOT NULL DEFAULT 'unknown',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    last_access TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE identity.device_permission (
    id SERIAL PRIMARY KEY,
    device_id INTEGER NOT NULL REFERENCES identity.device(id) ON DELETE CASCADE,
    permission_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, permission_type)
);

CREATE TABLE identity.device_session (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES identity.account(id) ON DELETE CASCADE,
    device_id INTEGER NOT NULL REFERENCES identity.device(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    expires_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE identity.alert_activation_setting (
    id SERIAL PRIMARY KEY,
    device_id INTEGER NOT NULL REFERENCES identity.device(id) ON DELETE CASCADE,
    activation_method VARCHAR(30) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, activation_method)
);

CREATE TABLE identity.user_preference (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL UNIQUE REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL DEFAULT 'es',
    theme VARCHAR(20) NOT NULL DEFAULT 'light',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- ALERT SERVICE TABLES (order matters for FK)
-- ============================================

CREATE TABLE alert.emergency_contact (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    contact_name VARCHAR(100) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    relationship VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE alert.alert (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    device_id INTEGER NOT NULL REFERENCES identity.device(id) ON DELETE CASCADE,
    alert_type VARCHAR(20) NOT NULL DEFAULT 'main',
    activation_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    message VARCHAR(500),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert.alert_contact (
    id SERIAL PRIMARY KEY,
    alert_id INTEGER NOT NULL REFERENCES alert.alert(id) ON DELETE CASCADE,
    emergency_contact_id INTEGER NOT NULL REFERENCES alert.emergency_contact(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    destination VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(alert_id, emergency_contact_id, channel)
);

CREATE TABLE alert.location_log (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    alert_id INTEGER REFERENCES alert.alert(id) ON DELETE SET NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    accuracy DECIMAL(6,2),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert.notification (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    alert_id INTEGER REFERENCES alert.alert(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_persistent BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert.evidence (
    id SERIAL PRIMARY KEY,
    alert_id INTEGER NOT NULL REFERENCES alert.alert(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert.frequent_location (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    notes VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE alert.alert_reminder (
    id SERIAL PRIMARY KEY,
    alert_id INTEGER NOT NULL REFERENCES alert.alert(id) ON DELETE CASCADE,
    reminder_type VARCHAR(20) NOT NULL,
    sequence_number INTEGER NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- RESOURCE SERVICE TABLES
-- ============================================

CREATE TABLE resource.zone (
    id SERIAL PRIMARY KEY,
    created_by_admin_id INTEGER,
    name VARCHAR(150) NOT NULL,
    zone_type VARCHAR(20) NOT NULL,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'medium',
    description VARCHAR(500),
    address VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    radius_meters DECIMAL(8,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE resource.zone_report (
    id SERIAL PRIMARY KEY,
    zone_id INTEGER NOT NULL REFERENCES resource.zone(id) ON DELETE CASCADE,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    classification VARCHAR(20) NOT NULL,
    comment VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewed_by_admin_id INTEGER,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(zone_id, user_profile_id)
);

CREATE TABLE resource.emergency_resource (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    telephone VARCHAR(30),
    secondary_telephone VARCHAR(30),
    email VARCHAR(150),
    address VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE resource.resource_call (
    id SERIAL PRIMARY KEY,
    emergency_resource_id INTEGER NOT NULL REFERENCES resource.emergency_resource(id) ON DELETE CASCADE,
    user_profile_id INTEGER NOT NULL REFERENCES identity.user_profile(id) ON DELETE CASCADE,
    alert_id INTEGER REFERENCES alert.alert(id) ON DELETE SET NULL,
    device_id INTEGER REFERENCES identity.device(id) ON DELETE SET NULL,
    telephone_dialed VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    duration_seconds INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ADMIN SERVICE TABLES
-- ============================================

CREATE TABLE admin.audit_log (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES identity.account(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin.user_report (
    id SERIAL PRIMARY KEY,
    reported_user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    reporter_user_id INTEGER REFERENCES identity.users(id) ON DELETE SET NULL,
    reason VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewed_by_admin_id INTEGER,
    reviewed_at TIMESTAMP,
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE admin.moderation_action (
    id SERIAL PRIMARY KEY,
    user_report_id INTEGER NOT NULL REFERENCES admin.user_report(id) ON DELETE CASCADE,
    admin_perfil_id INTEGER NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin.system_configuration (
    id SERIAL PRIMARY KEY,
    singleton_key VARCHAR(10) NOT NULL UNIQUE DEFAULT 'SYSTEM',
    sos_max_duration_minutes INTEGER NOT NULL,
    emergency_phone_number VARCHAR(20) NOT NULL,
    admin_email VARCHAR(150) NOT NULL,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_notification_priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    default_theme VARCHAR(20) NOT NULL DEFAULT 'light',
    updated_by_admin_id INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- SEED DATA
-- ============================================

INSERT INTO identity.role (id, name, description) VALUES
(1, 'user', 'Usuario estándar de la aplicación'),
(2, 'administrator', 'Administrador del sistema');

INSERT INTO identity.users (id, role_id, first_name, last_name, telephone, email, document_number, document_type, birthdate, created_at) VALUES
(1, 1, 'María', 'Pérez', '+57 311 000 0001', 'maria.perez@test.com', NULL, NULL, NULL, '2023-05-29 00:00:00'),
(2, 1, 'Ana', 'Gómez', '+57 312 000 0002', 'ana.gomez@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(3, 1, 'Laura', 'Torres', '+57 313 000 0003', 'laura.torres@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(4, 1, 'Sofía', 'Ruiz', '+57 314 000 0004', 'sofia.ruiz@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(5, 1, 'Camila', 'Ortiz', '+57 315 000 0005', 'camila.ortiz@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(6, 1, 'Diana', 'Morales', '+57 316 000 0006', 'diana.morales@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(7, 1, 'Paola', 'Vargas', '+57 317 000 0007', 'paola.vargas@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(8, 2, 'Admin', 'Principal', '+57 318 000 0008', 'admin@test.com', NULL, NULL, NULL, '2023-05-27 00:00:00'),
(9, 1, 'Raquel', '', '+57 320 000 0009', 'Raquel@gmail.com', NULL, NULL, NULL, '2026-09-17 00:00:00');

INSERT INTO identity.account (user_id, password_hash, status, last_access) VALUES
(1, 'Alerta@123', 'active', NULL),
(2, 'Alerta@123', 'inactive', NULL),
(3, 'Alerta@123', 'blocked', NULL),
(4, 'Alerta@123', 'blocked', NULL),
(5, 'Alerta@123', 'active', NULL),
(6, 'Alerta@123', 'blocked', NULL),
(7, 'Alerta@123', 'blocked', NULL),
(8, 'Admin@2026', 'active', NULL),
(9, 'Noruega@123', 'active', NULL);

INSERT INTO identity.user_profile (user_id, profile_photo_url, tutorial_completed, tutorial_seen_at, created_at, updated_at) VALUES
(1, NULL, FALSE, NULL, '2023-05-29 00:00:00', NULL),
(2, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(3, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(4, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(5, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(6, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(7, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(8, NULL, FALSE, NULL, '2023-05-27 00:00:00', NULL),
(9, NULL, FALSE, NULL, '2026-09-17 00:00:00', NULL);

INSERT INTO identity.device (account_id, device_uuid, brand, model, os_name, os_version, app_version, gps_status, status, last_access, created_at) VALUES
(1, 'device-001', 'Samsung', 'Galaxy A54', 'Android', '13', '1.0.0', 'active', 'active', NULL, '2023-05-29 00:00:00'),
(2, 'device-002', 'Apple', 'iPhone 13', 'iOS', '16.6', '1.0.0', 'active', 'active', NULL, '2023-05-27 00:00:00'),
(3, 'device-003', 'Xiaomi', 'Redmi Note 12', 'Android', '12', '1.0.0', 'inactive', 'inactive', NULL, '2023-05-27 00:00:00'),
(4, 'device-004', 'Motorola', 'G60', 'Android', '11', '1.0.0', 'active', 'active', NULL, '2023-05-27 00:00:00'),
(5, 'device-005', 'Apple', 'iPhone 11', 'iOS', '15.4', '1.0.0', 'active', 'blocked', NULL, '2023-05-27 00:00:00'),
(6, 'device-006', 'Samsung', 'Galaxy S22', 'Android', '13', '1.0.0', 'active', 'active', NULL, '2023-05-27 00:00:00'),
(7, 'device-007', 'Huawei', 'P30 Pro', 'Android', '10', '1.0.0', 'inactive', 'inactive', NULL, '2023-05-27 00:00:00'),
(8, 'device-008', 'Apple', 'iPhone SE', 'iOS', '17.0', '1.0.0', 'active', 'active', NULL, '2023-05-27 00:00:00');

INSERT INTO alert.emergency_contact (user_profile_id, contact_name, telephone, email, relationship, created_at, updated_at) VALUES
(1, 'Juan Pérez', '3111234567', NULL, 'Amigo', '2026-09-17 00:00:00', NULL),
(1, 'Ana Gómez', '300 111 0001', NULL, 'Hermano/a', '2026-09-17 00:00:00', NULL),
(1, 'Luis Díaz', '300 222 0002', NULL, 'Pareja', '2026-09-17 00:00:00', NULL),
(2, 'justin', '3226662738', NULL, 'Padre', '2026-09-17 00:00:00', NULL),
(2, 'justing', '3186866594', NULL, 'Hermano/a', '2026-09-17 00:00:00', NULL),
(9, 'DavidE', '573112082620', NULL, 'Amigo', '2026-09-17 00:00:00', NULL),
(9, 'Andrea', '573102307889', NULL, 'Amiga', '2026-09-17 00:00:00', NULL),
(9, 'Orueba', '12345678994976', NULL, 'Familia', '2026-09-17 21:35:10', NULL),
(9, 'Prueba', '3124928565', NULL, 'Femilia', '2026-09-22 02:38:25', NULL),
(9, 'Fotocopia', '573178863134', NULL, 'Vtvrv', '2026-09-24 21:55:16', NULL);

INSERT INTO alert.alert (id, user_profile_id, device_id, alert_type, activation_method, status, message, started_at, ended_at, cancelled_at, created_at) VALUES
(1, 1, 1, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-16 14:32:00', NULL, NULL, '2026-09-16 14:32:00'),
(2, 1, 1, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-16 09:20:00', NULL, NULL, '2026-09-16 09:20:00'),
(3, 2, 2, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-15 20:10:00', NULL, NULL, '2026-09-15 20:10:00'),
(4, 1, 1, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-15 22:05:00', NULL, NULL, '2026-09-15 22:05:00'),
(5, 1, 1, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-14 18:40:00', NULL, NULL, '2026-09-14 18:40:00'),
(6, 3, 3, 'main', 'panic_button', 'resolved', 'Robo a mano armada', '2026-09-14 11:05:00', NULL, NULL, '2026-09-14 11:05:00'),
(7, 4, 4, 'main', 'panic_button', 'resolved', 'Acoso en transporte', '2026-09-13 08:15:00', NULL, NULL, '2026-09-13 08:15:00'),
(8, 5, 5, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-12 16:55:00', NULL, NULL, '2026-09-12 16:55:00'),
(9, 6, 6, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-11 19:30:00', NULL, NULL, '2026-09-11 19:30:00'),
(10, 7, 7, 'main', 'panic_button', 'active', 'Acoso en calle', '2026-09-10 07:45:00', NULL, NULL, '2026-09-10 07:45:00');

INSERT INTO alert.location_log (user_profile_id, alert_id, latitude, longitude, accuracy, recorded_at) VALUES
(1, NULL, 4.711000, -74.072100, 10.00, '2026-09-16 14:32:00'),
(1, NULL, 4.709500, -74.069800, 15.00, '2026-09-16 14:33:00'),
(1, NULL, 4.708000, -74.075000, 12.00, '2026-09-16 14:34:00'),
(1, NULL, 4.706000, -74.073000, 8.00, '2026-09-16 14:35:00'),
(1, NULL, 4.704000, -74.071000, 20.00, '2026-09-16 14:36:00'),
(2, NULL, 4.705500, -74.076000, 10.00, '2026-09-15 20:10:00'),
(2, NULL, 4.710000, -74.070000, 15.00, '2026-09-15 20:11:00');

INSERT INTO resource.zone (id, created_by_admin_id, name, zone_type, risk_level, description, address, city, latitude, longitude, radius_meters, is_active, created_at, updated_at) VALUES
(1, NULL, 'Parque Santander', 'risk', 'high', 'Zona de alto riesgo', 'Calle 5 #10-20', 'Neiva', 4.711000, -74.072100, 500.00, TRUE, '2026-09-27 15:30:00', '2026-09-27 15:30:00'),
(2, NULL, 'Universidad Surcolombiana', 'safe', 'low', 'Zona segura', 'Carrera 5 #21-45', 'Neiva', 4.709500, -74.069800, 300.00, TRUE, '2026-09-27 17:45:00', '2026-09-27 17:45:00'),
(3, NULL, 'Parque Leesburg', 'risk', 'high', 'Zona de alto riesgo', 'Av. Caracas', 'Bogotá', 4.705500, -74.076000, 400.00, TRUE, '2026-09-26 12:00:00', '2026-09-26 12:00:00'),
(4, NULL, 'Centro Comercial San Pedro', 'risk', 'medium', 'Zona de riesgo medio', 'Calle 100 #15-20', 'Cali', 4.716000, -74.068000, 350.00, TRUE, '2026-09-27 13:30:00', '2026-09-27 13:30:00'),
(5, NULL, 'Parque de la Música', 'safe', 'low', 'Zona segura', 'Cra. 43 #1-20', 'Medellín', 4.721000, -74.064000, 250.00, TRUE, '2026-09-27 17:15:00', '2026-09-27 17:15:00'),
(6, NULL, 'Terminal de Transportes', 'risk', 'low', 'Zona de riesgo bajo', 'Calle 26 #45-60', 'Barranquilla', 4.706000, -74.075800, 450.00, TRUE, '2026-09-27 09:30:00', '2026-09-27 09:30:00');

INSERT INTO resource.emergency_resource (name, resource_type, telephone, secondary_telephone, email, address, city, latitude, longitude, description, is_active, created_at, updated_at) VALUES
('Línea de Emergencias 123', 'emergency_line', '123', NULL, NULL, NULL, 'Bogotá', NULL, NULL, 'Atención inmediata para cualquier emergencia.', TRUE, '2026-09-17 00:00:00', NULL),
('Línea Púrpura', 'emergency_line', '018000112137', NULL, NULL, NULL, 'Bogotá', NULL, NULL, 'Orientación y apoyo para mujeres en situación de violencia.', TRUE, '2026-09-17 00:00:00', NULL),
('Policía Nacional', 'police_station', '123', NULL, NULL, NULL, 'Bogotá', NULL, NULL, 'Reporta situaciones de riesgo o delitos en curso.', TRUE, '2026-09-17 00:00:00', NULL),
('Línea de Salud Mental', 'health_center', '106', NULL, NULL, NULL, 'Bogotá', NULL, NULL, 'Apoyo psicológico y contención emocional.', TRUE, '2026-09-17 00:00:00', NULL),
('Casa de la Mujer', 'support_organization', NULL, NULL, NULL, 'Calle 5 #21-45', 'Neiva', 4.704000, -74.071000, 'Atención integral y acompañamiento para mujeres.', TRUE, '2026-09-17 00:00:00', NULL),
('CAI Centro', 'police_post', NULL, NULL, NULL, 'Carrera 7 #12-30', 'Bogotá', 4.711500, -74.070000, 'Comando de Atención Inmediata de la Policía.', TRUE, '2026-09-17 00:00:00', NULL),
('Hospital La Victoria', 'hospital', NULL, NULL, NULL, 'Av. Caracas', 'Bogotá', 4.706000, -74.075500, 'Urgencias médicas y atención primaria.', TRUE, '2026-09-17 00:00:00', NULL),
('Defensoría del Pueblo', 'support_organization', NULL, NULL, NULL, 'Calle 15 #8-20', 'Bogotá', 4.720000, -74.065000, 'Orientación legal y defensa de derechos.', TRUE, '2026-09-17 00:00:00', NULL);

INSERT INTO admin.system_configuration (singleton_key, sos_max_duration_minutes, emergency_phone_number, admin_email, notifications_enabled, default_notification_priority, default_theme, updated_by_admin_id, created_at, updated_at) VALUES
('SYSTEM', 30, '123', 'admin@test.com', TRUE, 'normal', 'light', NULL, '2023-05-27 00:00:00', NULL);

-- Reset sequences
SELECT setval('identity.users_id_seq', (SELECT MAX(id) FROM identity.users));
SELECT setval('identity.role_id_seq', (SELECT MAX(id) FROM identity.role));
SELECT setval('identity.account_id_seq', (SELECT MAX(id) FROM identity.account));
SELECT setval('identity.user_profile_id_seq', (SELECT MAX(id) FROM identity.user_profile));
SELECT setval('identity.device_id_seq', (SELECT MAX(id) FROM identity.device));
SELECT setval('alert.alert_id_seq', (SELECT MAX(id) FROM alert.alert));
SELECT setval('alert.location_log_id_seq', (SELECT MAX(id) FROM alert.location_log));
SELECT setval('alert.emergency_contact_id_seq', (SELECT MAX(id) FROM alert.emergency_contact));
SELECT setval('resource.zone_id_seq', (SELECT MAX(id) FROM resource.zone));
SELECT setval('resource.emergency_resource_id_seq', (SELECT MAX(id) FROM resource.emergency_resource));
