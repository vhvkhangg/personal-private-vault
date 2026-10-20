-- V20__rag_snapshot_revisions.sql
-- Phase 18 Slice 1 Import & Snapshot (A+B+C)
-- Add pending and selected revision metadata columns to rag.external_file_sources

ALTER TABLE rag.external_file_sources
    ADD COLUMN IF NOT EXISTS selected_revision_id UUID,
    ADD COLUMN IF NOT EXISTS pending_revision_id UUID,
    ADD COLUMN IF NOT EXISTS pending_revision_hash VARCHAR(128),
    ADD COLUMN IF NOT EXISTS pending_byte_length BIGINT,
    ADD COLUMN IF NOT EXISTS pending_sealed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS expected_selected_revision_id UUID,
    ADD COLUMN IF NOT EXISTS expected_source_version BIGINT;

CREATE INDEX IF NOT EXISTS idx_rag_external_pending_rev ON rag.external_file_sources(pending_revision_id) WHERE pending_revision_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_rag_external_selected_rev ON rag.external_file_sources(selected_revision_id) WHERE selected_revision_id IS NOT NULL;

GRANT USAGE, SELECT ON SEQUENCE rag.external_file_sources_id_seq TO rag_runtime;
