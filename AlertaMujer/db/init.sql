-- ============================================
-- AlertaMujer - Base de datos PostgreSQL 16
-- Esquema + Seed a partir de db.json
-- ============================================

-- Crear schemas por servicio
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS alert;
CREATE SCHEMA IF NOT EXISTS resource;
CREATE SCHEMA IF NOT EXISTS admin;

-- ============================================
-- IDENTITY SERVICE (3001)
-- ============================================

CREATE TABLE identity.role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE identity.users (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    fecha_registro VARCHAR(20),
    alertas INTEGER DEFAULT 0,
    ultima_actividad VARCHAR(50),
    estado VARCHAR(30) DEFAULT 'Activa',
    rol VARCHAR(30) DEFAULT 'Usuaria',
    contacto_emergencia VARCHAR(100),
    avatar_color VARCHAR(20) DEFAULT '#7c3aed',
    role_id INTEGER DEFAULT 1 REFERENCES identity.role(id),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    document_number VARCHAR(30) DEFAULT '',
    document_type VARCHAR(20) DEFAULT '',
    birthdate DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE identity.account (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    last_access TIMESTAMP WITH TIME ZONE
);

CREATE TABLE identity.user_profile (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    profile_photo_url TEXT,
    tutorial_completed BOOLEAN DEFAULT FALSE,
    tutorial_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE identity.emergency_contact (
    id SERIAL PRIMARY KEY,
    user_profile_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    contact_name VARCHAR(100) NOT NULL,
    telephone VARCHAR(30) NOT NULL,
    relationship VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE identity.user_preference (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    preference_key VARCHAR(50) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE identity.recovery_request (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- ALERT SERVICE (3002)
-- ============================================

CREATE TABLE alert.alertas (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    nombre VARCHAR(100),
    descripcion TEXT,
    medio_activacion VARCHAR(50),
    tiempo VARCHAR(50),
    ubicacion VARCHAR(100),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    estado VARCHAR(30) DEFAULT 'Pendiente',
    user_profile_id INTEGER,
    device_id INTEGER,
    alert_type VARCHAR(20) DEFAULT 'main',
    activation_method VARCHAR(30),
    status VARCHAR(20) DEFAULT 'active',
    message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE alert.alert_contact (
    id SERIAL PRIMARY KEY,
    alerta_id INTEGER REFERENCES alert.alertas(id) ON DELETE CASCADE,
    contact_name VARCHAR(100),
    telephone VARCHAR(30),
    notified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE alert.notification (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    title VARCHAR(200),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE alert.alert_reminder (
    id SERIAL PRIMARY KEY,
    alerta_id INTEGER REFERENCES alert.alertas(id) ON DELETE CASCADE,
    reminder_type VARCHAR(50),
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE alert.alert_activation_setting (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    activation_method VARCHAR(50),
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE alert.ubicaciones (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    time VARCHAR(20),
    address VARCHAR(200),
    gps_signal VARCHAR(20),
    battery INTEGER,
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- RESOURCE SERVICE (3003)
-- ============================================

CREATE TABLE resource.zonas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ciudad VARCHAR(50),
    tipo VARCHAR(30) DEFAULT 'Riesgo Medio',
    alertas INTEGER DEFAULT 0,
    estado VARCHAR(20) DEFAULT 'Activa',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.zonas_manuales (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nivel VARCHAR(20),
    metodo VARCHAR(20),
    radio INTEGER,
    centro_lat DECIMAL(10, 7),
    centro_lng DECIMAL(10, 7),
    alertas_en_zona INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.puntos_mapa (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    lat DECIMAL(10, 7) NOT NULL,
    lng DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.frequent_places (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20),
    address VARCHAR(200),
    city VARCHAR(100),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.nearby_zones (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20),
    description TEXT,
    distance_km DECIMAL(8, 2),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    radius_meters INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.lineas_ayuda (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    numero VARCHAR(30),
    descripcion TEXT,
    disponibilidad VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.centros_ayuda (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(30),
    distancia_km DECIMAL(8, 2),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    abierto BOOLEAN DEFAULT TRUE,
    estado VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.recursos_guardados (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    ref_tipo VARCHAR(20),
    ref_id INTEGER,
    nombre VARCHAR(100),
    subtitulo VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.frequent_location (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    visit_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE resource.resource_call (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    resource_type VARCHAR(30),
    resource_id INTEGER,
    called_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- ADMIN SERVICE (3004)
-- ============================================

CREATE TABLE admin.reportes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE,
    ciudad VARCHAR(50),
    tipo VARCHAR(30),
    estado VARCHAR(20) DEFAULT 'Pendiente',
    generado_por VARCHAR(50),
    exportaciones INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.evidencias (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    tipo VARCHAR(20),
    tipo_alerta VARCHAR(20),
    nombre VARCHAR(150),
    tamanio VARCHAR(20),
    fecha VARCHAR(20),
    alerta VARCHAR(20),
    estado VARCHAR(20) DEFAULT 'En la nube',
    ubicacion JSONB,
    en_vivo BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.devices (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    os_version VARCHAR(30),
    type VARCHAR(10),
    imei VARCHAR(30),
    phone VARCHAR(30),
    status VARCHAR(20) DEFAULT 'Activo',
    last_sync VARCHAR(50),
    icon VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.device_permission (
    id SERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES admin.devices(id) ON DELETE CASCADE,
    permission_name VARCHAR(50),
    granted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.device_session (
    id SERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES admin.devices(id) ON DELETE CASCADE,
    session_token VARCHAR(255),
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.zone_report (
    id SERIAL PRIMARY KEY,
    zone_id INTEGER,
    report_type VARCHAR(30),
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.user_report (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    report_type VARCHAR(30),
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.moderation_action (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    action_type VARCHAR(30),
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.admin_perfil (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE CASCADE,
    permissions JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE admin.system_configuration (
    id SERIAL PRIMARY KEY,
    singleton_key VARCHAR(20) DEFAULT 'SYSTEM',
    sos_max_duration_minutes INTEGER DEFAULT 30,
    emergency_phone_number VARCHAR(20),
    admin_email VARCHAR(100),
    notifications_enabled BOOLEAN DEFAULT TRUE,
    default_notification_priority VARCHAR(20) DEFAULT 'normal',
    default_theme VARCHAR(20) DEFAULT 'light',
    updated_by_admin_perfil_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE admin.audit_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES identity.users(id) ON DELETE SET NULL,
    action VARCHAR(50),
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- SEED DATA (desde db.json)
-- ============================================

-- Roles
INSERT INTO identity.role (id, name, description) VALUES
(1, 'user', 'Usuario estándar de la aplicación'),
(2, 'administrator', 'Administrador del sistema');

-- Usuarios (contraseñas en texto plano - se migrarán a bcrypt en el backend)
INSERT INTO identity.users (id, nombre, email, correo, telefono, fecha_registro, alertas, ultima_actividad, estado, rol, contacto_emergencia, avatar_color, role_id, first_name, last_name, document_number, document_type, birthdate, created_at) VALUES
(1, 'María Pérez', 'maria.perez@test.com', 'maria.perez@test.com', '+57 311 000 0001', '29/05/2023', 7, 'hace 2 horas', 'Activa', 'Usuaria', 'Ana Gómez (+57 311 111 0001)', '#7c3aed', 1, 'María', 'Pérez', '', '', NULL, '2023-05-29T00:00:00.000Z'),
(2, 'Ana Gómez', 'ana.gomez@test.com', 'ana.gomez@test.com', '+57 312 000 0002', '27/05/2023', 2, 'hace 3 días', 'Inactiva', 'Usuaria', 'Luis Díaz (+57 312 111 0002)', '#a78bfa', 1, 'Ana', 'Gómez', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(3, 'Laura Torres', 'laura.torres@test.com', 'laura.torres@test.com', '+57 313 000 0003', '27/05/2023', 12, 'hace 1 semana', 'Bloqueada por Fraude', 'Usuaria', 'Pedro Flores (+57 313 111 0003)', '#ec4899', 1, 'Laura', 'Torres', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(4, 'Sofía Ruiz', 'sofia.ruiz@test.com', 'sofia.ruiz@test.com', '+57 314 000 0004', '27/05/2023', 9, 'hace 5 días', 'Bloqueada por Fraude', 'Usuaria', 'Rosa Gomez (+57 314 111 0004)', '#6d28d9', 1, 'Sofía', 'Ruiz', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(5, 'Camila Ortiz', 'camila.ortiz@test.com', 'camila.ortiz@test.com', '+57 315 000 0005', '27/05/2023', 3, 'hace 1 hora', 'Activa', 'Usuaria', 'José Tian (+57 315 111 0005)', '#7c3aed', 1, 'Camila', 'Ortiz', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(6, 'Diana Morales', 'diana.morales@test.com', 'diana.morales@test.com', '+57 316 000 0006', '27/05/2023', 15, 'hace 2 semanas', 'Bloqueada por Fraude', 'Usuaria', 'N/A', '#c4b5fd', 1, 'Diana', 'Morales', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(7, 'Paola Vargas', 'paola.vargas@test.com', 'paola.vargas@test.com', '+57 317 000 0007', '27/05/2023', 6, 'hace 4 días', 'Bloqueada por Fraude', 'Usuaria', 'María Flores (+57 317 111 0007)', '#ec4899', 1, 'Paola', 'Vargas', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(8, 'Admin Principal', 'admin@test.com', 'admin@test.com', '+57 318 000 0008', '27/05/2023', 0, 'hace 30 min', 'Activa', 'Admin', 'N/A', '#16a34a', 2, 'Admin', 'Principal', '', '', NULL, '2023-05-27T00:00:00.000Z'),
(9, 'Raquel', 'Raquel@gmail.com', 'Raquel@gmail.com', '+57 320 000 0009', '17/09/2026', 0, 'recién registrado', 'Activa', 'Usuaria', 'N/A', '#7c3aed', 1, 'Raquel', '', '', '', NULL, '2026-09-17T00:00:00.000Z');

-- Accounts (password_hash en texto plano - se migrarán a bcrypt)
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

-- User profiles
INSERT INTO identity.user_profile (user_id, profile_photo_url, tutorial_completed, tutorial_seen_at, created_at, updated_at) VALUES
(1, NULL, FALSE, NULL, '2023-05-29T00:00:00.000Z', NULL),
(2, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(3, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(4, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(5, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(6, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(7, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(8, NULL, FALSE, NULL, '2023-05-27T00:00:00.000Z', NULL),
(9, NULL, FALSE, NULL, '2026-09-17T00:00:00.000Z', NULL);

-- Emergency contacts (unificado: contactos + emergency_contact)
INSERT INTO identity.emergency_contact (user_profile_id, contact_name, telephone, relationship, created_at) VALUES
(1, 'Juan Pérez', '3111234567', 'Amigo', '2026-09-17T00:00:00.000Z'),
(9, 'DavidE', '573112082620', 'Amigo', '2026-09-17T00:00:00.000Z'),
(9, 'Antisecuestro, antiterrorismo y antiextorsion (F.F.M.M)', '14706868668', 'Servicio', '2026-09-17T00:00:00.000Z'),
(9, 'Andrea', '573102307889', 'Amiga', '2026-09-17T00:00:00.000Z'),
(9, 'Orueba', '12345678994976', 'Familia', '2026-09-17T21:35:10.073Z'),
(9, 'Daniela y miguel', '31248424954345', 'Esclava', '2026-09-17T22:16:01.228Z'),
(9, 'Prueba', '3124928565', 'Femilia', '2026-09-22T02:38:25.851Z'),
(9, 'Fotocopia', '573178863134', 'Vtvrv', '2026-09-24T21:55:16.072Z'),
(1, 'Ana Gómez', '300 111 0001', 'Hermano/a', '2026-09-17T00:00:00.000Z'),
(1, 'Luis Díaz', '300 222 0002', 'Pareja', '2026-09-17T00:00:00.000Z'),
(2, 'justin', '3226662738', 'Padre', '2026-09-17T00:00:00.000Z'),
(2, 'justing', '3186866594', 'Hermano/a', '2026-09-17T00:00:00.000Z');

-- Alertas
INSERT INTO alert.alertas (id, usuario_id, nombre, descripcion, medio_activacion, tiempo, ubicacion, lat, lng, estado, user_profile_id, device_id, alert_type, activation_method, status, message, started_at, ended_at, cancelled_at, created_at) VALUES
(1, 1, 'María Pérez', 'Pánico activado', 'Botón de pánico', 'Hace 2 min', 'Centro', 4.711, -74.0721, 'Atendida', 1, 1, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-16T14:32:00.000Z', NULL, NULL, '2026-09-16T14:32:00.000Z'),
(2, 1, 'María Pérez', 'Emergencia médica', 'Botón de pánico', 'Hace 2 min', 'Centro', 4.7118, -74.0715, 'Atendida', 1, 1, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-16T09:20:00.000Z', NULL, NULL, '2026-09-16T09:20:00.000Z'),
(3, 2, 'Ana Gómez', 'Pánico activado', 'Botón de pánico', 'Hace 3 min', 'Centro', 4.7105, -74.0728, 'Atendida', 2, 2, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-15T20:10:00.000Z', NULL, NULL, '2026-09-15T20:10:00.000Z'),
(4, 1, 'María Pérez', 'Emergencia médica', 'Botón de pánico', 'Hace 5 min', 'Chapinero', 4.72, -74.065, 'Atendida', 1, 1, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-15T22:05:00.000Z', NULL, NULL, '2026-09-15T22:05:00.000Z'),
(5, 1, 'María Pérez', 'Pánico activado', 'Botón de pánico', 'Hace 6 min', 'Centro', 4.7112, -74.0719, 'Atendida', 1, 1, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-14T18:40:00.000Z', NULL, NULL, '2026-09-14T18:40:00.000Z'),
(6, 3, 'Laura Torres', 'Robo a mano armada', 'Botón de pánico', 'Hace 8 min', 'Calle 80', 4.716, -74.068, 'Atendida', 3, 3, 'main', 'panic_button', 'resolved', 'Robo a mano armada', '2026-09-14T11:05:00.000Z', NULL, NULL, '2026-09-14T11:05:00.000Z'),
(7, 4, 'Sofía Ruiz', 'Acoso en transporte', 'Botón de pánico', 'Hace 10 min', 'Av. Caracas', 4.7055, -74.076, 'Atendida', 4, 4, 'main', 'panic_button', 'resolved', 'Acoso en transporte', '2026-09-13T08:15:00.000Z', NULL, NULL, '2026-09-13T08:15:00.000Z'),
(8, 5, 'Camila Ortiz', 'Emergencia médica', 'Botón de pánico', 'Hace 12 min', 'Chapinero', 4.721, -74.064, 'Atendida', 5, 5, 'main', 'panic_button', 'resolved', 'Emergencia médica', '2026-09-12T16:55:00.000Z', NULL, NULL, '2026-09-12T16:55:00.000Z'),
(9, 6, 'Diana Morales', 'Pánico activado', 'Botón de pánico', 'Hace 15 min', 'Centro', 4.7108, -74.0724, 'Atendida', 6, 6, 'main', 'panic_button', 'resolved', 'Pánico activado', '2026-09-11T19:30:00.000Z', NULL, NULL, '2026-09-11T19:30:00.000Z'),
(10, 7, 'Paola Vargas', 'Acoso en calle', 'Botón de pánico', 'Hace 18 min', 'Av. Caracas', 4.706, -74.0758, 'Pendiente', 7, 7, 'main', 'panic_button', 'active', 'Acoso en calle', '2026-09-10T07:45:00.000Z', NULL, NULL, '2026-09-10T07:45:00.000Z');

-- Ubicaciones
INSERT INTO alert.ubicaciones (usuario_id, time, address, gps_signal, battery, lat, lng) VALUES
(1, '11:30 AM', 'Calle Principal 123, Ciudad', 'Fuerte', 85, 4.711, -74.0721),
(1, '11:15 AM', 'Parque Central', 'Fuerte', 82, 4.7095, -74.0698),
(1, '10:45 AM', 'Centro Comercial', 'Moderada', 79, 4.708, -74.075),
(1, '10:30 AM', 'Av. Libertad 456', 'Fuerte', 76, 4.706, -74.073),
(1, '9:30 AM', 'Zona Norte, Barrio El Prado', 'Débil', 70, 4.704, -74.071),
(2, '12:00 PM', 'Av. Caracas, Centro', 'Fuerte', 90, 4.7055, -74.076),
(2, '11:40 AM', 'Estación Norte', 'Moderada', 88, 4.71, -74.07);

-- Zonas
INSERT INTO resource.zonas (id, nombre, ciudad, tipo, alertas, estado, updated_at) VALUES
(1, 'Parque Santander', 'Neiva', 'Riesgo Alto', 37, 'Activa', '2026-09-27T15:30:00.000Z'),
(2, 'Universidad Surcolombiana', 'Neiva', 'Segura', 3, 'Activa', '2026-09-27T17:45:00.000Z'),
(3, 'Parque Leesburg', 'Bogotá', 'Riesgo Alto', 28, 'Activa', '2026-09-26T12:00:00.000Z'),
(4, 'Centro Comercial San Pedro', 'Cali', 'Riesgo Medio', 14, 'Revisión', '2026-09-27T13:30:00.000Z'),
(5, 'Parque de la Música', 'Medellín', 'Segura', 2, 'Activa', '2026-09-27T17:15:00.000Z'),
(6, 'Terminal de Transportes', 'Barranquilla', 'Riesgo Bajo', 8, 'Activa', '2026-09-27T09:30:00.000Z');

-- Zonas manuales
INSERT INTO resource.zonas_manuales (nombre, nivel, metodo, radio, centro_lat, centro_lng, alertas_en_zona) VALUES
('Zona Chapinero', 'Medio', 'area', 280, 4.719, -74.06566666666666, 3),
('Zona Av. Caracas', 'Bajo', 'area', 180, 4.70575, -74.07589999999999, 2);

-- Puntos del mapa
INSERT INTO resource.puntos_mapa (tipo, nombre, lat, lng) VALUES
('CAI', 'CAI Centro', 4.7115, -74.07),
('CAI', 'CAI Norte', 4.7185, -74.066),
('Hospital', 'Hospital La Victoria', 4.706, -74.0755),
('Hospital', 'Hospital Simón Bolívar', 4.721, -74.064);

-- Frequent places
INSERT INTO resource.frequent_places (user_id, name, type, address, city, lat, lng, is_main) VALUES
(1, 'Casa', 'home', 'Carrera 5 #21-45', 'Neiva, Huila', 4.711, -74.0721, TRUE),
(1, 'Trabajo', 'work', 'Calle 10 #8-20', 'Neiva, Huila', 4.7095, -74.0698, FALSE),
(1, 'SENA', 'study', 'Neiva, Huila', 'Neiva, Huila', 4.708, -74.075, FALSE);

-- Nearby zones
INSERT INTO resource.nearby_zones (name, type, description, distance_km, lat, lng, radius_meters) VALUES
('Zona segura', 'safe', 'Parque Santander', 0.8, 4.7112, -74.0715, 200),
('Zona de riesgo', 'risk', 'Sector X', 1.2, 4.706, -74.073, 150),
('Centro de ayuda', 'help', 'Casa de la Mujer', 1.5, 4.704, -74.071, 100);

-- Líneas de ayuda
INSERT INTO resource.lineas_ayuda (tipo, nombre, numero, descripcion, disponibilidad) VALUES
('emergencia', 'Línea de Emergencias 123', '123', 'Atención inmediata para cualquier emergencia.', 'Disponible 24/7'),
('mujeres', 'Línea Púrpura', '018000112137', 'Orientación y apoyo para mujeres en situación de violencia.', 'Disponible 24/7'),
('policia', 'Policía Nacional', '123', 'Reporta situaciones de riesgo o delitos en curso.', 'Disponible 24/7'),
('saludMental', 'Línea de Salud Mental', '106', 'Apoyo psicológico y contención emocional.', 'Disponible 24/7');

-- Centros de ayuda
INSERT INTO resource.centros_ayuda (nombre, descripcion, categoria, distancia_km, lat, lng, abierto, estado) VALUES
('Casa de la Mujer', 'Atención integral y acompañamiento para mujeres.', 'mujeres', 1.5, 4.704, -74.071, TRUE, 'Atención al público'),
('CAI Centro', 'Comando de Atención Inmediata de la Policía.', 'seguridad', 0.8, 4.7115, -74.07, TRUE, 'Servicio permanente'),
('Hospital La Victoria', 'Urgencias médicas y atención primaria.', 'salud', 1.2, 4.706, -74.0755, TRUE, 'Urgencias 24 horas'),
('Defensoría del Pueblo', 'Orientación legal y defensa de derechos.', 'legal', 2.1, 4.72, -74.065, FALSE, 'Cierra a las 5:00 pm');

-- Recursos guardados
INSERT INTO resource.recursos_guardados (usuario_id, ref_tipo, ref_id, nombre, subtitulo) VALUES
(2, 'linea', 1, 'Línea de Emergencias 123', 'Línea · 24/7'),
(2, 'linea', 2, 'Línea Púrpura', 'Línea · 24/7');

-- Reportes
INSERT INTO admin.reportes (id, nombre, fecha, ciudad, tipo, estado, generado_por, exportaciones) VALUES
(1, 'Reporte de SOS - Septiembre 2026', '2026-09-26T10:30:00.000Z', 'Neiva', 'SOS', 'Pendiente', 'Administrador', 0),
(2, 'Reporte de Robos - Septiembre 2026', '2026-09-25T16:15:00.000Z', 'Bogotá', 'Robo', 'Atendida', 'Administrador', 3),
(3, 'Reporte de Acoso - Septiembre 2026', '2026-09-24T11:45:00.000Z', 'Cali', 'Acoso', 'Atendida', 'Administrador', 2),
(4, 'Reporte Médico - Septiembre 2026', '2026-09-23T09:20:00.000Z', 'Neiva', 'Médico', 'Pendiente', 'Administrador', 0),
(5, 'Reporte de SOS - Septiembre 2026', '2026-09-22T20:10:00.000Z', 'Medellín', 'SOS', 'Atendida', 'Administrador', 4),
(6, 'Reporte de Alertas - Septiembre 2026', '2026-09-21T14:00:00.000Z', 'Barranquilla', 'SOS', 'Cerrada', 'Administrador', 5),
(7, 'Reporte de Robos - Septiembre 2026', '2026-09-19T18:40:00.000Z', 'Bogotá', 'Robo', 'Cerrada', 'Administrador', 1),
(8, 'Reporte de Acoso - Septiembre 2026', '2026-09-18T08:15:00.000Z', 'Cali', 'Acoso', 'Pendiente', 'Administrador', 0),
(9, 'Reporte Médico - Septiembre 2026', '2026-09-16T22:05:00.000Z', 'Medellín', 'Médico', 'Atendida', 'Administrador', 2),
(10, 'Reporte de SOS - Septiembre 2026', '2026-09-15T13:30:00.000Z', 'Neiva', 'SOS', 'Cerrada', 'Administrador', 6),
(11, 'Reporte de Robos - Septiembre 2026', '2026-09-12T17:25:00.000Z', 'Barranquilla', 'Robo', 'Atendida', 'Administrador', 1),
(12, 'Reporte de Acoso - Septiembre 2026', '2026-09-10T07:45:00.000Z', 'Bogotá', 'Acoso', 'Cerrada', 'Administrador', 3),
(13, 'Reporte Médico - Septiembre 2026', '2026-09-08T19:50:00.000Z', 'Cali', 'Médico', 'Cerrada', 'Administrador', 2),
(14, 'Reporte de SOS - Septiembre 2026', '2026-09-05T12:10:00.000Z', 'Medellín', 'SOS', 'Atendida', 'Administrador', 4),
(15, 'Reporte de Alertas - Agosto 2026', '2026-08-28T10:00:00.000Z', 'Neiva', 'SOS', 'Cerrada', 'Administrador', 7),
(16, 'Reporte de Robos - Agosto 2026', '2026-08-22T15:35:00.000Z', 'Bogotá', 'Robo', 'Cerrada', 'Administrador', 2),
(17, 'Reporte de Acoso - Agosto 2026', '2026-08-17T09:05:00.000Z', 'Cali', 'Acoso', 'Atendida', 'Administrador', 1),
(18, 'Reporte Médico - Agosto 2026', '2026-08-11T21:20:00.000Z', 'Barranquilla', 'Médico', 'Cerrada', 'Administrador', 0),
(19, 'Reporte de SOS - Agosto 2026', '2026-08-04T06:55:00.000Z', 'Medellín', 'SOS', 'Cerrada', 'Administrador', 5),
(20, 'Reporte de Alertas - Julio 2026', '2026-07-23T10:30:00.000Z', 'Neiva', 'SOS', 'Cerrada', 'Administrador', 8);

-- Evidencias
INSERT INTO admin.evidencias (id, usuario_id, tipo, tipo_alerta, nombre, tamanio, fecha, alerta, estado, ubicacion, en_vivo) VALUES
(1, 1, 'video', 'SOS', 'SOS_2026-06-13_09-14.mp4', '15.3 MB', '13 jun 2026', '#001', 'En la nube', '{"lat": 4.711, "lng": -74.0721, "direccion": "Centro"}', FALSE),
(2, 1, 'foto', 'SOS', 'SOS_2026-06-13_09-14.jpg', '5.9 MB', '13 jun 2026', '#001', 'En la nube', '{"lat": 4.711, "lng": -74.0721, "direccion": "Centro"}', FALSE),
(3, 1, 'audio', 'Medical', 'Medical_2026-06-13_09-20.mp3', '1.9 MB', '13 jun 2026', '#002', 'En la nube', '{"lat": 4.7118, "lng": -74.0715, "direccion": "Centro"}', FALSE),
(4, 2, 'video', 'SOS', 'SOS_2026-06-12_14-32.mp4', '9.2 MB', '12 jun 2026', '#003', 'En la nube', '{"lat": 4.7105, "lng": -74.0728, "direccion": "Centro"}', FALSE),
(5, 3, 'foto', 'Robo', 'Robo_2026-06-12_08-05.jpg', '5.7 MB', '12 jun 2026', '#006', 'Pendiente', '{"lat": 4.716, "lng": -74.068, "direccion": "Calle 80"}', FALSE),
(6, 4, 'audio', 'Acoso', 'Acoso_2026-06-10_17-45.mp3', '2.7 MB', '10 jun 2026', '#007', 'En la nube', '{"lat": 4.7055, "lng": -74.076, "direccion": "Av. Caracas"}', FALSE);

-- System configuration
INSERT INTO admin.system_configuration (singleton_key, sos_max_duration_minutes, emergency_phone_number, admin_email, notifications_enabled, default_notification_priority, default_theme, updated_by_admin_perfil_id, created_at, updated_at) VALUES
('SYSTEM', 30, '123', 'admin@test.com', TRUE, 'normal', 'light', NULL, '2023-05-27T00:00:00.000Z', NULL);

-- Resetear secuencias
SELECT setval('identity.users_id_seq', (SELECT MAX(id) FROM identity.users));
SELECT setval('identity.role_id_seq', (SELECT MAX(id) FROM identity.role));
SELECT setval('identity.account_id_seq', (SELECT MAX(id) FROM identity.account));
SELECT setval('identity.user_profile_id_seq', (SELECT MAX(id) FROM identity.user_profile));
SELECT setval('identity.emergency_contact_id_seq', (SELECT MAX(id) FROM identity.emergency_contact));
SELECT setval('alert.alertas_id_seq', (SELECT MAX(id) FROM alert.alertas));
SELECT setval('alert.ubicaciones_id_seq', (SELECT MAX(id) FROM alert.ubicaciones));
SELECT setval('resource.zonas_id_seq', (SELECT MAX(id) FROM resource.zonas));
SELECT setval('resource.zonas_manuales_id_seq', (SELECT MAX(id) FROM resource.zonas_manuales));
SELECT setval('resource.puntos_mapa_id_seq', (SELECT MAX(id) FROM resource.puntos_mapa));
SELECT setval('resource.frequent_places_id_seq', (SELECT MAX(id) FROM resource.frequent_places));
SELECT setval('resource.nearby_zones_id_seq', (SELECT MAX(id) FROM resource.nearby_zones));
SELECT setval('resource.lineas_ayuda_id_seq', (SELECT MAX(id) FROM resource.lineas_ayuda));
SELECT setval('resource.centros_ayuda_id_seq', (SELECT MAX(id) FROM resource.centros_ayuda));
SELECT setval('resource.recursos_guardados_id_seq', (SELECT MAX(id) FROM resource.recursos_guardados));
SELECT setval('admin.reportes_id_seq', (SELECT MAX(id) FROM admin.reportes));
SELECT setval('admin.evidencias_id_seq', (SELECT MAX(id) FROM admin.evidencias));
