-- ============================================
-- FASE 3: Escalabilidad y Mantenimiento
-- AlertaMujer - PostgreSQL 16
-- ============================================
-- Este script debe ejecutarse DESPUÉS de phase2_improvements.sql
-- Ejecutar: psql -U postgres -d alertamujer -f phase3_improvements.sql
-- ============================================

-- ============================================
-- NOTA IMPORTANTE: Particionamiento requiere:
-- 1. PostgreSQL 11+ (ya cumplido con PostgreSQL 16)
-- 2. La tabla principal debe estar vacía o migrar datos existentes
-- 3. Requiere recrear la tabla como particionada
-- ============================================

-- ============================================
-- 1. PARTICIONAMIENTO POR FECHAS
-- alert.location_log y admin.audit_log
-- ============================================

-- ============================================
-- PARTICIONAMIENTO DE alert.location_log
-- ============================================

-- Paso 1: Crear tabla particionada (reemplazar la existente)
-- NOTA: Esto requiere migrar datos existentes primero

-- Crear tabla particionada por mes
CREATE TABLE alert.location_log_partitioned (
    id BIGSERIAL,
    user_profile_id BIGINT NOT NULL,
    alert_id INTEGER,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    accuracy DECIMAL(6,2),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (recorded_at);

-- Crear particiones para los últimos 6 meses + futuros
-- Ajustar las fechas según necesidad actual

-- Partición para datos históricos (antes de 2026-09)
CREATE TABLE alert.location_log_historical PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM (MINVALUE) TO ('2026-09-01');

-- Partición para septiembre 2026
CREATE TABLE alert.location_log_2026_09 PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

-- Partición para octubre 2026
CREATE TABLE alert.location_log_2026_10 PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

-- Partición para noviembre 2026
CREATE TABLE alert.location_log_2026_11 PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

-- Partición para diciembre 2026
CREATE TABLE alert.location_log_2026_12 PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- Partición para enero 2027
CREATE TABLE alert.location_log_2027_01 PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2027-01-01') TO ('2027-02-01');

-- Partición para datos futuros (después de enero 2027)
CREATE TABLE alert.location_log_future PARTITION OF alert.location_log_partitioned
    FOR VALUES FROM ('2027-02-01') TO (MAXVALUE);

-- Crear índices en la tabla particionada
CREATE INDEX idx_location_log_partitioned_user_profile_id ON alert.location_log_partitioned(user_profile_id);
CREATE INDEX idx_location_log_partitioned_alert_id ON alert.location_log_partitioned(alert_id);
CREATE INDEX idx_location_log_partitioned_recorded_at ON alert.location_log_partitioned(recorded_at DESC);

-- Crear trigger para updated_at en tabla particionada
CREATE TRIGGER trigger_location_log_partitioned_updated_at
    BEFORE UPDATE ON alert.location_log_partitioned
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- PARTICIONAMIENTO DE admin.audit_log
-- ============================================

-- Paso 1: Crear tabla particionada (reemplazar la existente)
-- NOTA: Esto requiere migrar datos existentes primero

-- Crear tabla particionada por mes
CREATE TABLE admin.audit_log_partitioned (
    id BIGSERIAL,
    account_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- Crear particiones para los últimos 6 meses + futuros
-- Ajustar las fechas según necesidad actual

-- Partición para datos históricos (antes de 2026-09)
CREATE TABLE admin.audit_log_historical PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM (MINVALUE) TO ('2026-09-01');

-- Partición para septiembre 2026
CREATE TABLE admin.audit_log_2026_09 PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

-- Partición para octubre 2026
CREATE TABLE admin.audit_log_2026_10 PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

-- Partición para noviembre 2026
CREATE TABLE admin.audit_log_2026_11 PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

-- Partición para diciembre 2026
CREATE TABLE admin.audit_log_2026_12 PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- Partición para enero 2027
CREATE TABLE admin.audit_log_2027_01 PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2027-01-01') TO ('2027-02-01');

-- Partición para datos futuros (después de enero 2027)
CREATE TABLE admin.audit_log_future PARTITION OF admin.audit_log_partitioned
    FOR VALUES FROM ('2027-02-01') TO (MAXVALUE);

-- Crear índices en la tabla particionada
CREATE INDEX idx_audit_log_partitioned_account_id ON admin.audit_log_partitioned(account_id);
CREATE INDEX idx_audit_log_partitioned_entity_type ON admin.audit_log_partitioned(entity_type);
CREATE INDEX idx_audit_log_partitioned_created_at ON admin.audit_log_partitioned(created_at DESC);

-- Crear trigger para updated_at en tabla particionada
CREATE TRIGGER trigger_audit_log_partitioned_updated_at
    BEFORE UPDATE ON admin.audit_log_partitioned
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 2. POLÍTICAS DE RETENCIÓN Y ARCHIVADO
-- Script ETL para mover datos históricos a cold storage
-- ============================================

-- ============================================
-- Crear tablas de archivado (cold storage)
-- Estas tablas no tienen índices para ahorrar espacio
-- ============================================

-- Tabla de archivado para location_log
CREATE TABLE alert.location_log_archive (
    id BIGINT,
    user_profile_id BIGINT,
    alert_id INTEGER,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    accuracy DECIMAL(6,2),
    recorded_at TIMESTAMP,
    updated_at TIMESTAMP,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de archivado para audit_log
CREATE TABLE admin.audit_log_archive (
    id BIGINT,
    account_id BIGINT,
    action VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id INTEGER,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Función para archivar location_log (> 6 meses)
-- ============================================

CREATE OR REPLACE FUNCTION archive_location_logs()
RETURNS INTEGER AS $$
DECLARE
    archived_count INTEGER;
BEGIN
    -- Insertar datos antiguos en tabla de archivado
    INSERT INTO alert.location_log_archive (id, user_profile_id, alert_id, latitude, longitude, accuracy, recorded_at, updated_at)
    SELECT id, user_profile_id, alert_id, latitude, longitude, accuracy, recorded_at, updated_at
    FROM alert.location_log
    WHERE recorded_at < CURRENT_TIMESTAMP - INTERVAL '6 months';
    
    GET DIAGNOSTICS archived_count = ROW_COUNT;
    
    -- Eliminar datos antiguos de la tabla principal
    DELETE FROM alert.location_log
    WHERE recorded_at < CURRENT_TIMESTAMP - INTERVAL '6 months';
    
    RETURN archived_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- Función para archivar audit_log (> 1 año)
-- ============================================

CREATE OR REPLACE FUNCTION archive_audit_logs()
RETURNS INTEGER AS $$
DECLARE
    archived_count INTEGER;
BEGIN
    -- Insertar datos antiguos en tabla de archivado
    INSERT INTO admin.audit_log_archive (id, account_id, action, entity_type, entity_id, description, ip_address, created_at, updated_at)
    SELECT id, account_id, action, entity_type, entity_id, description, ip_address, created_at, updated_at
    FROM admin.audit_log
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';
    
    GET DIAGNOSTICS archived_count = ROW_COUNT;
    
    -- Eliminar datos antiguos de la tabla principal
    DELETE FROM admin.audit_log
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';
    
    RETURN archived_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- Función para crear particiones mensuales automáticamente
-- ============================================

CREATE OR REPLACE FUNCTION create_monthly_partition(table_name TEXT, partition_date DATE)
RETURNS TEXT AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    -- Calcular nombre de partición
    partition_name := table_name || '_' || to_char(partition_date, 'YYYY_MM');
    
    -- Calcular rangos de fecha
    start_date := date_trunc('month', partition_date);
    end_date := start_date + INTERVAL '1 month';
    
    -- Crear partición
    EXECUTE format('CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
        partition_name, table_name, start_date, end_date);
    
    -- Crear índices en la partición
    EXECUTE format('CREATE INDEX idx_%s_user_profile_id ON %I(user_profile_id)',
        partition_name, partition_name);
    EXECUTE format('CREATE INDEX idx_%s_recorded_at ON %I(recorded_at DESC)',
        partition_name, partition_name);
    
    RETURN partition_name;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- Job programado para archivado automático
-- Requiere pg_cron (extensión de PostgreSQL)
-- ============================================

-- Habilitar extensión pg_cron (si está disponible)
-- CREATE EXTENSION IF NOT EXISTS pg_cron;

-- Programar archivado de location_log cada mes (día 1 a las 2 AM)
-- SELECT cron.schedule('archive-location-logs', '0 2 1 * *', 'SELECT archive_location_logs();');

-- Programar archivado de audit_log cada mes (día 1 a las 3 AM)
-- SELECT cron.schedule('archive-audit-logs', '0 3 1 * *', 'SELECT archive_audit_logs();');

-- Programar creación de particiones mensuales (día 25 a las 4 AM)
-- SELECT cron.schedule('create-monthly-partitions', '0 4 25 * *', 'SELECT create_monthly_partition(''alert.location_log_partitioned'', CURRENT_DATE + INTERVAL ''1 month'');');

-- ============================================
-- 3. VISTAS MATERIALIZADAS PARA CONSULTAS RÁPIDAS
-- ============================================

-- Vista materializada para alertas activas
CREATE MATERIALIZED VIEW alert.active_alerts AS
SELECT 
    a.id,
    a.user_profile_id,
    a.device_id,
    a.alert_type,
    a.activation_method,
    a.status,
    a.message,
    a.latitude,
    a.longitude,
    a.started_at,
    u.first_name,
    u.last_name,
    u.telephone
FROM alert.alert a
JOIN identity.user_profile up ON a.user_profile_id = up.id
JOIN identity.users u ON up.user_id = u.id
WHERE a.status = 'active';

CREATE UNIQUE INDEX idx_active_alerts_id ON alert.active_alerts(id);

-- Vista materializada para zonas por ciudad
CREATE MATERIALIZED VIEW resource.zones_by_city AS
SELECT 
    city,
    COUNT(*) FILTER (WHERE zone_type = 'risk') AS risk_zones,
    COUNT(*) FILTER (WHERE zone_type = 'safe') AS safe_zones,
    COUNT(*) FILTER (WHERE zone_type = 'help') AS help_zones,
    COUNT(*) FILTER (WHERE zone_type = 'policia') AS police_zones,
    COUNT(*) FILTER (WHERE zone_type = 'asistencia') AS assistance_zones,
    COUNT(*) AS total_zones
FROM resource.zone
WHERE is_active = true
GROUP BY city;

CREATE UNIQUE INDEX idx_zones_by_city ON resource.zones_by_city(city);

-- Función para refrescar vistas materializadas
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY alert.active_alerts;
    REFRESH MATERIALIZED VIEW CONCURRENTLY resource.zones_by_city;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- FIN DE FASE 3
-- ============================================
-- Verificar que todo se aplicó correctamente:
-- SELECT tablename FROM pg_tables WHERE tablename LIKE '%partitioned%' OR tablename LIKE '%archive%';
-- SELECT proname FROM pg_proc WHERE proname LIKE '%archive%' OR proname LIKE '%partition%';
-- ============================================

-- ============================================
-- INSTRUCCIONES PARA MIGRACIÓN A TABLAS PARTICIONADAS
-- ============================================
-- 1. Hacer backup de la base de datos completa
-- 2. Renombrar tabla original: ALTER TABLE alert.location_log RENAME TO alert.location_log_old;
-- 3. Renombrar tabla particionada: ALTER TABLE alert.location_log_partitioned RENAME TO alert.location_log;
-- 4. Migrar datos de la tabla old a la particionada:
--    INSERT INTO alert.location_log SELECT * FROM alert.location_log_old;
-- 5. Verificar que los datos se distribuyeron correctamente en las particiones
-- 6. Eliminar tabla old: DROP TABLE alert.location_log_old;
-- 7. Repetir para admin.audit_log
-- 8. Actualizar la aplicación para usar las nuevas tablas
-- ============================================

-- ============================================
-- INSTRUCCIONES PARA ARCHIVADO AUTOMÁTICO
-- ============================================
-- 1. Instalar extensión pg_cron: CREATE EXTENSION pg_cron;
-- 2. Programar jobs de archivado usando cron.schedule()
-- 3. Monitorear el proceso de archivado
-- 4. Verificar que los datos archivados sean correctos
-- 5. Configurar alertas si el archivado falla
-- ============================================
