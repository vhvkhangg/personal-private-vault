-- Slice 1 corrective scope: independent selected descriptor and persistent integrity denial.
-- No existing selected snapshot is inferred/adopted from a file manifest.
ALTER TABLE rag.external_file_sources
    ADD COLUMN pending_consent_context VARCHAR(160),
    ADD COLUMN approved_pending_revision_id UUID,
    ADD COLUMN selected_revision_hash VARCHAR(128),
    ADD COLUMN selected_byte_length BIGINT,
    ADD COLUMN snapshot_integrity_failed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE rag.external_file_sources
    ADD CONSTRAINT ck_rag_snapshot_pending_descriptor CHECK (
        (pending_revision_id IS NULL AND pending_revision_hash IS NULL AND pending_byte_length IS NULL
            AND pending_sealed_at IS NULL AND expected_source_version IS NULL AND expected_selected_revision_id IS NULL)
        OR (pending_revision_id IS NOT NULL AND pending_revision_hash IS NOT NULL AND pending_byte_length IS NOT NULL
            AND expected_source_version IS NOT NULL AND pending_revision_hash ~ '^[a-fA-F0-9]{64}$'
            AND pending_byte_length >= 0 AND pending_sealed_at IS NOT NULL AND expected_source_version > 0)),
    ADD CONSTRAINT ck_rag_snapshot_selected_length CHECK (selected_byte_length IS NULL OR selected_byte_length >= 0);
