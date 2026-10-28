--liquibase formatted sql

-- Evidencias con archivo real. Antes solo se guardaba un texto file_url y ningún cliente
-- podía subir archivos. Los archivos viven en disco (volumen) y solo se sirven por la API
-- a la dueña de la alerta; aquí se guardan sus metadatos y su huella SHA-256 para poder
-- demostrar que no fueron alterados.

--changeset alerta-mujer-team:v1.3.0-evidence-file-metadata
ALTER TABLE alert.evidence
    ADD COLUMN file_name    VARCHAR(255),
    ADD COLUMN content_type VARCHAR(100),
    ADD COLUMN size_bytes   BIGINT,
    ADD COLUMN sha256       CHAR(64),
    ADD COLUMN storage_key  VARCHAR(100);
ALTER TABLE alert.evidence ADD CONSTRAINT uk_evidence_storage_key UNIQUE (storage_key);
ALTER TABLE alert.evidence ADD CONSTRAINT chk_evidence_media_type
    CHECK (media_type IN ('photo', 'video', 'audio', 'document'));
ALTER TABLE alert.evidence ADD CONSTRAINT chk_evidence_size
    CHECK (size_bytes IS NULL OR size_bytes >= 0);
CREATE INDEX IF NOT EXISTS idx_evidence_created_at ON alert.evidence (created_at DESC);
--rollback ALTER TABLE alert.evidence DROP CONSTRAINT IF EXISTS chk_evidence_size, DROP CONSTRAINT IF EXISTS chk_evidence_media_type, DROP CONSTRAINT IF EXISTS uk_evidence_storage_key, DROP COLUMN IF EXISTS storage_key, DROP COLUMN IF EXISTS sha256, DROP COLUMN IF EXISTS size_bytes, DROP COLUMN IF EXISTS content_type, DROP COLUMN IF EXISTS file_name;
--rollback DROP INDEX IF EXISTS alert.idx_evidence_created_at;

--changeset alerta-mujer-team:v1.3.0-evidence-audit
CREATE TRIGGER trg_audit_evidence AFTER INSERT OR UPDATE OR DELETE ON alert.evidence
    FOR EACH ROW EXECUTE FUNCTION admin.log_row_change('EVIDENCE');
--rollback DROP TRIGGER IF EXISTS trg_audit_evidence ON alert.evidence;
