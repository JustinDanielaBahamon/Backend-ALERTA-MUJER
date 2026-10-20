--liquibase formatted sql

-- Estado de revisión de cada evidencia, que la administración marca desde el panel.
-- Las evidencias existentes quedan pendientes de revisión.

--changeset alerta-mujer-team:v1.6.0-evidence-review-status
ALTER TABLE alert.evidence ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'pending';
ALTER TABLE alert.evidence ADD CONSTRAINT chk_evidence_review_status
    CHECK (review_status IN ('pending', 'verified', 'rejected'));
--rollback ALTER TABLE alert.evidence DROP CONSTRAINT IF EXISTS chk_evidence_review_status;
--rollback ALTER TABLE alert.evidence DROP COLUMN IF EXISTS review_status;
