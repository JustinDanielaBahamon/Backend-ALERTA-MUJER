--liquibase formatted sql

-- Gravedad de cada reporte de usuaria, que la moderación usa para priorizar y filtrar.
-- Los reportes existentes quedan con gravedad media.

--changeset alerta-mujer-team:v1.6.0-user-report-severity
ALTER TABLE admin.user_report ADD COLUMN severity VARCHAR(10) NOT NULL DEFAULT 'medium';
ALTER TABLE admin.user_report ADD CONSTRAINT chk_user_report_severity
    CHECK (severity IN ('low', 'medium', 'high', 'critical'));
--rollback ALTER TABLE admin.user_report DROP CONSTRAINT IF EXISTS chk_user_report_severity;
--rollback ALTER TABLE admin.user_report DROP COLUMN IF EXISTS severity;
