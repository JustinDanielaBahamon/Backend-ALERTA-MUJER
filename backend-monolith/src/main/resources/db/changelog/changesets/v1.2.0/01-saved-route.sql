--liquibase formatted sql

-- Recorridos guardados por la usuaria. Antes vivían solo en localStorage (web) y AsyncStorage
-- (móvil), así que no se compartían entre dispositivos y se perdían al borrar datos.
-- Reglas de negocio (iguales en web y móvil), garantizadas también aquí por si un cliente
-- las salta: máximo 4 recorridos y máximo 2 "estrella" por usuaria.

--changeset alerta-mujer-team:v1.2.0-saved-route-table
CREATE TABLE alert.saved_route (
    id                BIGSERIAL PRIMARY KEY,
    user_profile_id   BIGINT       NOT NULL,
    name              VARCHAR(150) NOT NULL,
    start_label       VARCHAR(150),
    end_label         VARCHAR(150),
    city              VARCHAR(100),
    department        VARCHAR(100),
    country           VARCHAR(100),
    distance_km       NUMERIC(8, 2),
    estimated_minutes INTEGER,
    points            JSONB        NOT NULL,
    is_starred        BOOLEAN      NOT NULL DEFAULT false,
    is_manual         BOOLEAN      NOT NULL DEFAULT true,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP             DEFAULT now(),
    CONSTRAINT fk_saved_route_user_profile FOREIGN KEY (user_profile_id) REFERENCES identity.user_profile (id),
    CONSTRAINT chk_saved_route_points CHECK (jsonb_typeof(points) = 'array' AND jsonb_array_length(points) BETWEEN 2 AND 5000),
    CONSTRAINT chk_saved_route_distance CHECK (distance_km IS NULL OR distance_km >= 0),
    CONSTRAINT chk_saved_route_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0)
);
CREATE INDEX idx_saved_route_user_profile_id ON alert.saved_route (user_profile_id, created_at DESC);
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON alert.saved_route
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
--rollback DROP TABLE alert.saved_route;

--changeset alerta-mujer-team:v1.2.0-saved-route-limits splitStatements:false
CREATE OR REPLACE FUNCTION alert.enforce_saved_route_limits()
RETURNS TRIGGER AS $$
BEGIN
    -- Serializa las escrituras de la misma usuaria para que dos peticiones simultáneas
    -- no superen el límite.
    PERFORM pg_advisory_xact_lock(hashtext('saved_route'), NEW.user_profile_id::INTEGER);

    IF TG_OP = 'INSERT' AND
       (SELECT count(*) FROM alert.saved_route WHERE user_profile_id = NEW.user_profile_id) >= 4 THEN
        RAISE EXCEPTION 'Máximo 4 recorridos por usuaria' USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.is_starred AND (TG_OP = 'INSERT' OR NOT OLD.is_starred) AND
       (SELECT count(*) FROM alert.saved_route
         WHERE user_profile_id = NEW.user_profile_id AND is_starred AND id <> NEW.id) >= 2 THEN
        RAISE EXCEPTION 'Máximo 2 recorridos estrella por usuaria' USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_saved_route_limits BEFORE INSERT OR UPDATE OF is_starred, user_profile_id ON alert.saved_route
    FOR EACH ROW EXECUTE FUNCTION alert.enforce_saved_route_limits();
--rollback DROP TRIGGER IF EXISTS trg_saved_route_limits ON alert.saved_route;
--rollback DROP FUNCTION IF EXISTS alert.enforce_saved_route_limits();
