--liquibase formatted sql

-- Protección automática: al salir de uno de sus lugares frecuentes, su celular inicia solo una
-- vigilancia sin destino (un trayecto con mode = 'auto'). Solo alerta si se pierde la señal o si se
-- detiene en una zona de riesgo y no responde.
--   safe_trip.mode:           'trip' (lo inició ella, con destino) o 'auto'.
--   safe_trip.low_battery_at: su celular avisó que tenía menos del 10 % de batería.
--   protection_device:        celulares con la protección encendida; cada uno tiene su propia llave
--                             (solo se guarda su hash SHA-256) para funcionar aunque su sesión haya vencido.

--changeset alerta-mujer-team:v1.6.0-auto-protection
ALTER TABLE alert.safe_trip
    ADD COLUMN mode           VARCHAR(10) NOT NULL DEFAULT 'trip',
    ADD COLUMN low_battery_at TIMESTAMP;

CREATE TABLE alert.protection_device (
    id              BIGSERIAL PRIMARY KEY,
    user_profile_id BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    device_id       BIGINT      NOT NULL,
    key_hash        VARCHAR(64) NOT NULL UNIQUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_protection_device_profile ON alert.protection_device (user_profile_id, device_id);
--rollback DROP TABLE alert.protection_device; ALTER TABLE alert.safe_trip DROP COLUMN low_battery_at, DROP COLUMN mode;
