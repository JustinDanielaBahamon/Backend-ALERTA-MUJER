--liquibase formatted sql

-- Informes de alertas que la administración guarda y exporta a PDF o Excel (RF13.4).
-- Un informe incluye las alertas elegidas (alert_ids) o, si no se eligió ninguna, las que
-- cumplen los filtros guardados (rango de fechas y estado de la alerta).

--changeset alerta-mujer-team:v1.6.0-alert-report
CREATE TABLE admin.alert_report (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    city                VARCHAR(100),
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending',
    user_label          VARCHAR(200),
    event               VARCHAR(200),
    description         TEXT,
    format              VARCHAR(10)  NOT NULL DEFAULT 'PDF',
    alert_ids           TEXT,
    from_date           TIMESTAMP,
    to_date             TIMESTAMP,
    alert_status        VARCHAR(20),
    export_count        INTEGER      NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT REFERENCES identity.users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT chk_alert_report_status CHECK (status IN ('pending', 'attended', 'closed')),
    CONSTRAINT chk_alert_report_format CHECK (format IN ('PDF', 'EXCEL')),
    CONSTRAINT chk_alert_report_export_count CHECK (export_count >= 0)
);
CREATE INDEX idx_alert_report_created_at ON admin.alert_report (created_at DESC);
GRANT SELECT, INSERT, UPDATE, DELETE ON admin.alert_report TO alerta_app;
GRANT USAGE, SELECT ON SEQUENCE admin.alert_report_id_seq TO alerta_app;
--rollback DROP TABLE IF EXISTS admin.alert_report;
