--liquibase formatted sql

-- Quién abrió el seguimiento de una alerta (enlace del correo o app del contacto), para que la
-- usuaria sepa que la están viendo. "visitor" es un identificador anónimo del navegador o la app.

--changeset alerta-mujer-team:v1.6.0-tracking-view
CREATE TABLE notification.tracking_view (
    alert_id   BIGINT      NOT NULL REFERENCES alert.alert (id),
    visitor    VARCHAR(64) NOT NULL,
    first_seen TIMESTAMP   NOT NULL DEFAULT now(),
    last_seen  TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (alert_id, visitor)
);
--rollback DROP TABLE notification.tracking_view;
