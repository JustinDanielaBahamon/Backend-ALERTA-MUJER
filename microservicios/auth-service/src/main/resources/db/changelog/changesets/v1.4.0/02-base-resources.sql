--liquibase formatted sql

-- Líneas de ayuda que muestra la pantalla de Asistencia del móvil (emergencias, violencia
-- y salud mental). Se cargan en todos los ambientes, también en producción, porque sin
-- ellas la pantalla queda vacía. No duplica filas: en dev/test ya vienen del seed.

--changeset alerta-mujer-team:v1.4.0-base-help-lines
INSERT INTO resource.emergency_resource (name, resource_type, telephone, city, description, is_active)
SELECT v.name, v.resource_type, v.telephone, v.city, v.description, TRUE
FROM (VALUES
    ('Línea de Emergencias 123', 'emergency_line', '123', 'Bogotá', 'Atención inmediata para cualquier emergencia.'),
    ('Línea Púrpura', 'emergency_line', '018000112137', 'Bogotá', 'Orientación y apoyo para mujeres en situación de violencia.'),
    ('Línea de Salud Mental', 'health_center', '106', 'Bogotá', 'Apoyo psicológico y contención emocional.')
) AS v(name, resource_type, telephone, city, description)
WHERE NOT EXISTS (SELECT 1 FROM resource.emergency_resource r WHERE r.name = v.name);
--rollback DELETE FROM resource.emergency_resource WHERE name IN ('Línea de Emergencias 123', 'Línea Púrpura', 'Línea de Salud Mental') AND latitude IS NULL;
