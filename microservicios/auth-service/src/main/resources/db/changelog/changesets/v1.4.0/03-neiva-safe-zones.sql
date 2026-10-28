--liquibase formatted sql

-- Zonas seguras y puntos de ayuda reales de Neiva (Huila). Coordenadas tomadas de
-- OpenStreetMap (Nominatim) el 15/10/2026. Solo incluye lugares de apoyo (policía,
-- salud, justicia y protección); las zonas de riesgo debe definirlas el equipo con una
-- fuente oficial. No duplica filas: omite las que ya existan con el mismo nombre y ciudad.

--changeset alerta-mujer-team:v1.4.0-neiva-safe-zones
INSERT INTO resource.zone (name, zone_type, risk_level, description, address, city, latitude, longitude, radius_meters, is_active)
SELECT v.name, v.zone_type, 'low', v.description, v.address, 'Neiva', v.lat, v.lng, v.radius, TRUE
FROM (VALUES
    ('Policía Metropolitana de Neiva', 'policia', 'Sede de la Policía Metropolitana.', 'Carrera 13, Tenerife', 2.938839, -75.283843, 200),
    ('Comando de Policía Andalucía', 'policia', 'Comando de policía en el sur de la ciudad.', 'Avenida Carrera 7, Andalucía', 2.907323, -75.280725, 200),
    ('CAI Altico', 'policia', 'Comando de Atención Inmediata.', 'Carrera 14, El Altico', 2.927750, -75.280094, 150),
    ('CAI Los Guaduales', 'policia', 'Comando de Atención Inmediata.', 'Diagonal 20, Los Guaduales', 2.942035, -75.261135, 150),
    ('CAI Santa Rosa', 'policia', 'Comando de Atención Inmediata.', 'Calle 81, Santa Rosa', 2.975192, -75.287761, 150),
    ('CAI Timanco', 'policia', 'Comando de Atención Inmediata.', 'Calle 16 Sur, Timanco', 2.912671, -75.274417, 150),
    ('CAI Las Palmas', 'policia', 'Comando de Atención Inmediata.', 'Calle 19, Villa Rosa', 2.940521, -75.249328, 150),
    ('Hospital Universitario de Neiva', 'asistencia', 'Urgencias 24 horas.', 'Carrera 13, El Altico', 2.932576, -75.281027, 250),
    ('Clínica Medilaser', 'asistencia', 'Urgencias y atención médica.', 'Avenida Carrera 7, El Altico', 2.930793, -75.288044, 150),
    ('Clínica UROS', 'asistencia', 'Urgencias y atención médica.', 'Carrera 6, Quirinal', 2.934504, -75.289987, 150),
    ('Clínica Belo Horizonte', 'asistencia', 'Atención médica.', 'Calle 8, Ipanema', 2.933939, -75.247633, 150),
    ('Bomberos Neiva', 'asistencia', 'Estación de bomberos.', 'Carrera 7A, José Eustasio Rivera', 2.933154, -75.287764, 150),
    ('Fiscalía General de la Nación - Neiva', 'asistencia', 'Recepción de denuncias.', 'Carrera 8, Diego de Ospina', 2.926934, -75.285683, 150),
    ('Palacio de Justicia de Neiva', 'asistencia', 'Juzgados y atención judicial.', 'Calle 4 #6-99', 2.925169, -75.289608, 150),
    ('ICBF Regional Huila', 'asistencia', 'Instituto Colombiano de Bienestar Familiar.', 'Carrera 1F, El Caracolí', 2.932735, -75.298124, 150),
    ('Alcaldía de Neiva', 'asistencia', 'Atención a la ciudadanía.', 'Calle 10, El Altico', 2.928809, -75.289283, 150),
    ('Universidad Surcolombiana', 'safe', 'Campus con vigilancia.', 'Avenida Circunvalar, Los Andaquíes', 2.942997, -75.301699, 300)
) AS v(name, zone_type, description, address, lat, lng, radius)
WHERE NOT EXISTS (
    SELECT 1 FROM resource.zone z WHERE z.name = v.name AND z.city = 'Neiva'
);
--rollback DELETE FROM resource.zone WHERE city = 'Neiva' AND risk_level = 'low' AND description IS NOT NULL AND created_by_admin_id IS NULL AND name IN ('Policía Metropolitana de Neiva', 'Comando de Policía Andalucía', 'CAI Altico', 'CAI Los Guaduales', 'CAI Santa Rosa', 'CAI Timanco', 'CAI Las Palmas', 'Hospital Universitario de Neiva', 'Clínica Medilaser', 'Clínica UROS', 'Clínica Belo Horizonte', 'Bomberos Neiva', 'Fiscalía General de la Nación - Neiva', 'Palacio de Justicia de Neiva', 'ICBF Regional Huila', 'Alcaldía de Neiva', 'Universidad Surcolombiana');
