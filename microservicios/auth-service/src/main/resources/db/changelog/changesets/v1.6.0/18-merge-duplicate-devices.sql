--liquibase formatted sql

-- Une los celulares duplicados de una misma cuenta (misma marca, modelo, nombre y sistema): se
-- creaban al reinstalar la app o borrar sus datos. Se conserva el usado más recientemente; las
-- alertas, sesiones, llamadas y trayectos de los duplicados pasan a él y los duplicados se borran.

--changeset alerta-mujer-team:v1.6.0-merge-duplicate-devices
CREATE TEMP TABLE device_merge ON COMMIT DROP AS
SELECT id, conservar FROM (
    SELECT id, first_value(id) OVER (
        PARTITION BY account_id, lower(trim(brand)), lower(trim(model)),
                     lower(trim(coalesce(device_name, ''))), lower(trim(coalesce(os_name, '')))
        ORDER BY last_access DESC NULLS LAST, id DESC) AS conservar
    FROM identity.device
    WHERE brand IS NOT NULL AND model IS NOT NULL AND lower(coalesce(os_name, '')) <> 'web'
) g WHERE id <> conservar;

UPDATE alert.alert a SET device_id = m.conservar FROM device_merge m WHERE a.device_id = m.id;
UPDATE resource.resource_call r SET device_id = m.conservar FROM device_merge m WHERE r.device_id = m.id;
UPDATE alert.safe_trip t SET device_id = m.conservar FROM device_merge m WHERE t.device_id = m.id;
UPDATE identity.device_session s SET device_id = m.conservar FROM device_merge m WHERE s.device_id = m.id;
-- Permisos y ajustes por dispositivo: el que se conserva ya tiene los suyos
DELETE FROM identity.device_permission p USING device_merge m WHERE p.device_id = m.id;
DELETE FROM identity.alert_activation_setting s USING device_merge m WHERE s.device_id = m.id;
DELETE FROM identity.device d USING device_merge m WHERE d.id = m.id;
--rollback SELECT 1;
